
package bookbridge.model;

import java.time.LocalDate;

public class LoanRecord {

    private final String loanId;
    private final Member member;
    private final LibraryBook book;
    private final LocalDate dueDate;
    private final int requestedDays;
    private final int lateDays;
    private LoanStatus status;

    public LoanRecord(String loanId, Member member,
                      LibraryBook book, LocalDate dueDate,
                      int requestedDays, int lateDays) {

        if (loanId == null || loanId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Loan ID cannot be blank.");
        }

        if (member == null || book == null || dueDate == null) {
            throw new IllegalArgumentException(
                    "Member, book, and due date are required.");
        }

        if (requestedDays < 1 || requestedDays > 30
                || requestedDays > member.getMaximumLoanDays()) {
            throw new IllegalArgumentException(
                    "Requested days exceed the allowed limit.");
        }

        if (lateDays < 0) {
            throw new IllegalArgumentException(
                    "Late days cannot be negative.");
        }

        this.loanId = loanId.trim();
        this.member = member;
        this.book = book;
        this.dueDate = dueDate;
        this.requestedDays = requestedDays;
        this.lateDays = lateDays;
        this.status = LoanStatus.ACTIVE;
    }

    public String getLoanId() {
        return loanId;
    }

    public Member getMember() {
        return member;
    }

    public LibraryBook getBook() {
        return book;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getRequestedDays() {
        return requestedDays;
    }

    public int getLateDays() {
        return lateDays;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public int getEstimatedFine() {
        return member.calculateLateCharge(lateDays);
    }

    public void markReturned() {
        if (status != LoanStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Loan has already been returned.");
        }

        status = LoanStatus.RETURNED;
    }
}
