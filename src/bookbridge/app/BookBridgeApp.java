
package bookbridge.app;

import bookbridge.model.*;
import bookbridge.service.LibraryService;
import bookbridge.service.Result;
import bookbridge.exception.*;

import java.util.List;

public class BookBridgeApp {

    public static void main(String[] args) {

        LibraryService service = new LibraryService();

        System.out.println("========== BOOKBRIDGE TSA3 ==========");

        try {
            // Register members and books
            service.registerMember(new LibraryMember(
                    "M001", "Ana Cruz", MembershipType.STUDENT));
            service.registerMember(new LibraryMember(
                    "M002", "Ben Lim", MembershipType.FACULTY));
            service.registerMember(new LibraryMember(
                    "M003", "Cara Reyes", MembershipType.GUEST));

            service.addBook(new LibraryBook("978001", "Java Basics"));
            service.addBook(new LibraryBook("978002", "Database Systems"));
            service.addBook(new LibraryBook("978003", "Web Design"));

            System.out.println("\n--- INITIAL SETUP ---");
            System.out.println("Members registered: "
                    + service.listMembers().size());
            System.out.println("Books added: "
                    + service.listBooks().size());

            // TEST 1: Successful loan
            System.out.println("\nTEST 1 - SUCCESSFUL LOAN");
            LoanRecord loan1 = service.issueLoan(
                    "M001", "978001", 10, 2);
            System.out.println("Loan created: " + loan1.getLoanId());
            System.out.println("Fine: PHP " + loan1.getEstimatedFine());

            // TEST 2: Duplicate member
            System.out.println("\nTEST 2 - DUPLICATE MEMBER");
            int membersBefore = service.listMembers().size();

            try {
                service.registerMember(new LibraryMember(
                        "M001", "Another Student",
                        MembershipType.STUDENT));
                System.out.println("FAIL: Duplicate was accepted.");
            } catch (DuplicateEntityException e) {
                System.out.println("Handled: " + e.getMessage());
            }

            System.out.println("Members unchanged: "
                    + (membersBefore == service.listMembers().size()));

            // TEST 3: Unavailable book
            System.out.println("\nTEST 3 - UNAVAILABLE BOOK");
            int loansBefore = service.getActiveLoanCount();

            try {
                service.issueLoan("M002", "978001", 5, 0);
                System.out.println("FAIL: Unavailable book was loaned.");
            } catch (ItemUnavailableException e) {
                System.out.println("Handled: " + e.getMessage());
            }

            System.out.println("Active loans unchanged: "
                    + (loansBefore == service.getActiveLoanCount()));
            System.out.println("Checked-out count unchanged: "
                    + (service.getCheckedOutBookCount() == 1));

            // TEST 4: Loan limit exceeded
            System.out.println("\nTEST 4 - LOAN LIMIT");
            int historyBefore = service.listLoanHistory().size();

            try {
                service.issueLoan("M003", "978003", 10, 0);
                System.out.println("FAIL: Invalid loan was accepted.");
            } catch (LoanLimitExceededException e) {
                System.out.println("Handled: " + e.getMessage());
            }

            System.out.println("History unchanged: "
                    + (historyBefore == service.listLoanHistory().size()));
            System.out.println("Book remains available: "
                    + service.findBook("978003").isAvailable());

            // TEST 5: Missing search and generic Result<T>
            System.out.println("\nTEST 5 - MISSING SEARCH");
            LibraryBook missing = service.findBook("999999");

            Result<String> searchResult = Result.success(
                    missing == null ? "Book not found" : "Book found");

            System.out.println(searchResult.getValue());

            // TEST 6: Reservation queue
            System.out.println("\nTEST 6 - RESERVATION QUEUE");
            service.reserveBook("M002", "978001", 5);

            System.out.println("Reservations: "
                    + service.getReservationCount());

            LoanRecord waiting = service.processNextReservation();

            System.out.println("Processed while unavailable: "
                    + (waiting != null));
            System.out.println("Queue preserved: "
                    + (service.getReservationCount() == 1));

            // TEST 7: Return and reservation processing
            System.out.println("\nTEST 7 - RETURN AND RESERVE");
            service.returnBook(loan1.getLoanId());

            LoanRecord reservedLoan = service.processNextReservation();

            System.out.println("Reservation processed: "
                    + (reservedLoan != null));
            System.out.println("Reservations remaining: "
                    + service.getReservationCount());

            // TEST 8: Aggregate verification
            System.out.println("\nTEST 8 - AGGREGATE SUMMARY");
            System.out.println("Available books: "
                    + service.getAvailableBookCount());
            System.out.println("Checked-out books: "
                    + service.getCheckedOutBookCount());
            System.out.println("Active loans: "
                    + service.getActiveLoanCount());
            System.out.println("Total estimated fines: PHP "
                    + service.getTotalEstimatedFines());

            // TEST 9: Empty reservation queue
            System.out.println("\nTEST 9 - EMPTY QUEUE");
            LoanRecord emptyResult = service.processNextReservation();

            System.out.println("Empty queue returns null: "
                    + (emptyResult == null));
            System.out.println("Queue remains empty: "
                    + (service.getReservationCount() == 0));

            // TEST 10: Sorting and tie-breaking
            System.out.println("\nTEST 10 - SORTING");

            LoanRecord secondLoan = service.issueLoan(
                    "M003", "978003", 7, 0);

            List<LoanRecord> sorted = service.getSortedLoanHistory();

            System.out.println("Sorted loan IDs:");
            for (LoanRecord loan : sorted) {
                System.out.println(loan.getLoanId()
                        + " | Due: " + loan.getDueDate()
                        + " | Status: " + loan.getStatus());
            }

            System.out.println("Sorting returned "
                    + sorted.size() + " records.");

            System.out.println("\nALL TESTS COMPLETED");

        } catch (DuplicateEntityException
                 | ItemUnavailableException
                 | LoanLimitExceededException e) {

            System.out.println("Operation failed: " + e.getMessage());

        } catch (IllegalArgumentException e) {

            System.out.println("Invalid input: " + e.getMessage());
        }
    }
}
