
package bookbridge.service;

import bookbridge.model.*;
import bookbridge.exception.*;

import java.time.LocalDate;
import java.util.*;

public class LibraryService {

    // Collections for managing library records
    private final Map<String, LibraryBook> books = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final Map<String, LoanRecord> activeLoans = new HashMap<>();

    private final Queue<Reservation> reservations = new LinkedList<>();
    private final Set<String> checkedOutIsbns = new HashSet<>();
    private final List<LoanRecord> loanHistory = new ArrayList<>();

    private int nextLoanId = 1;

    // Register a library member
    public void registerMember(Member member)
            throws DuplicateEntityException {

        if (member == null) {
            throw new IllegalArgumentException(
                    "Member is required.");
        }

        String id = member.getMemberId();

        if (members.containsKey(id)) {
            throw new DuplicateEntityException(
                    "Member ID already exists: " + id);
        }

        members.put(id, member);
    }

    // Add a book to the catalog
    public void addBook(LibraryBook book)
            throws DuplicateEntityException {

        if (book == null) {
            throw new IllegalArgumentException(
                    "Book is required.");
        }

        String isbn = book.getIsbn();

        if (books.containsKey(isbn)) {
            throw new DuplicateEntityException(
                    "ISBN already exists: " + isbn);
        }

        books.put(isbn, book);
    }

    // Issue a loan after checking the borrowing rules
    public LoanRecord issueLoan(String memberId, String isbn,
                                int requestedDays, int lateDays)
            throws ItemUnavailableException,
                   LoanLimitExceededException {

        Member member = members.get(memberId);
        LibraryBook book = books.get(isbn);

        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member or book was not found.");
        }

        if (requestedDays < 1 || requestedDays > 30) {
            throw new IllegalArgumentException(
                    "Requested days must be from 1 to 30.");
        }

        if (lateDays < 0) {
            throw new IllegalArgumentException(
                    "Late days cannot be negative.");
        }

        if (!book.isAvailable() || checkedOutIsbns.contains(isbn)) {
            throw new ItemUnavailableException(
                    "Book is currently unavailable: " + isbn);
        }

        if (requestedDays > member.getMaximumLoanDays()) {
            throw new LoanLimitExceededException(
                    "Requested days exceed the membership limit.");
        }

        String loanId = String.format("L%03d", nextLoanId);

        LoanRecord loan = new LoanRecord(
                loanId, member, book,
                LocalDate.now().plusDays(requestedDays),
                requestedDays, lateDays);

        // Update records after successful validation
        book.markCheckedOut();
        checkedOutIsbns.add(isbn);
        activeLoans.put(loanId, loan);
        loanHistory.add(loan);

        nextLoanId++;

        return loan;
    }

    // Return an active loan
    public void returnBook(String loanId) {

        LoanRecord loan = activeLoans.get(loanId);

        if (loan == null) {
            throw new IllegalArgumentException(
                    "Active loan was not found.");
        }

        loan.markReturned();
        loan.getBook().markReturned();

        checkedOutIsbns.remove(loan.getBook().getIsbn());
        activeLoans.remove(loanId);
    }

    // Add a reservation to the waiting queue
    public void reserveBook(String memberId, String isbn,
                            int requestedDays) {

        Member member = members.get(memberId);
        LibraryBook book = books.get(isbn);

        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member or book was not found.");
        }

        if (requestedDays < 1 || requestedDays > 30
                || requestedDays > member.getMaximumLoanDays()) {
            throw new IllegalArgumentException(
                    "Invalid reservation loan period.");
        }

        reservations.add(
                new Reservation(member, book, requestedDays));
    }

    // Process the next reservation if its book is available
    public LoanRecord processNextReservation()
            throws ItemUnavailableException,
                   LoanLimitExceededException {

        if (reservations.isEmpty()) {
            return null;
        }

        Reservation next = reservations.peek();

        if (!next.getBook().isAvailable()) {
            return null;
        }

        LoanRecord loan = issueLoan(
                next.getMember().getMemberId(),
                next.getBook().getIsbn(),
                next.getRequestedDays(), 0);

        reservations.remove();
        return loan;
    }

    // Find records by their identifiers
    public LibraryBook findBook(String isbn) {
        return books.get(isbn);
    }

    public Member findMember(String memberId) {
        return members.get(memberId);
    }

    public LoanRecord findActiveLoan(String loanId) {
        return activeLoans.get(loanId);
    }

    // Search books using a title keyword
    public List<LibraryBook> searchBooksByTitle(String keyword) {

        if (keyword == null) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be null.");
        }

        List<LibraryBook> results = new ArrayList<>();

        for (LibraryBook book : books.values()) {
            if (book.getTitle().toLowerCase()
                    .contains(keyword.toLowerCase())) {
                results.add(book);
            }
        }

        return results;
    }

    // Sort overdue active loans first, then due date and loan ID
    public List<LoanRecord> getSortedLoanHistory() {

        List<LoanRecord> sorted = new ArrayList<>(loanHistory);

        sorted.sort(
                Comparator
                    .comparing(
                        (LoanRecord loan) ->
                            loan.getStatus() == LoanStatus.ACTIVE
                            && loan.getDueDate().isBefore(LocalDate.now())
                    )
                    .reversed()
                    .thenComparing(LoanRecord::getDueDate)
                    .thenComparing(LoanRecord::getLoanId)
        );

        return sorted;
    }

    // Filter loans by status
    public List<LoanRecord> getLoansByStatus(LoanStatus status) {

        List<LoanRecord> results = new ArrayList<>();

        for (LoanRecord loan : loanHistory) {
            if (loan.getStatus() == status) {
                results.add(loan);
            }
        }

        return results;
    }

    // Filter members by membership type
    public List<Member> getMembersByType(MembershipType type) {

        List<Member> results = new ArrayList<>();

        for (Member member : members.values()) {
            if (member.getMembershipType() == type) {
                results.add(member);
            }
        }

        return results;
    }

    // Count available books
    public int getAvailableBookCount() {

        int count = 0;

        for (LibraryBook book : books.values()) {
            if (book.isAvailable()) {
                count++;
            }
        }

        return count;
    }

    public int getCheckedOutBookCount() {
        return checkedOutIsbns.size();
    }

    public int getActiveLoanCount() {
        return activeLoans.size();
    }

    public int getReservationCount() {
        return reservations.size();
    }

    // Count active loans by membership type
    public int getActiveLoansByType(MembershipType type) {

        int count = 0;

        for (LoanRecord loan : activeLoans.values()) {
            if (loan.getMember().getMembershipType() == type) {
                count++;
            }
        }

        return count;
    }

    // Calculate total estimated fines from loan history
    public int getTotalEstimatedFines() {

        int total = 0;

        for (LoanRecord loan : loanHistory) {
            total += loan.getEstimatedFine();
        }

        return total;
    }

    // Return defensive copies of stored records
    public List<LibraryBook> listBooks() {
        return new ArrayList<>(books.values());
    }

    public List<Member> listMembers() {
        return new ArrayList<>(members.values());
    }

    public List<LoanRecord> listLoanHistory() {
        return new ArrayList<>(loanHistory);
    }
}
