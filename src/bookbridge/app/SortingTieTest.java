
package bookbridge.app;

import bookbridge.model.*;
import bookbridge.service.LibraryService;
import java.util.List;

public class SortingTieTest {

    public static void main(String[] args) throws Exception {

        LibraryService service = new LibraryService();

        service.registerMember(new LibraryMember(
                "M101", "Test Student", MembershipType.STUDENT));

        service.addBook(new LibraryBook("ISBN101", "Book One"));
        service.addBook(new LibraryBook("ISBN102", "Book Two"));

        // Both loans have the same requested period and due date
        LoanRecord first = service.issueLoan(
                "M101", "ISBN101", 7, 0);

        LoanRecord second = service.issueLoan(
                "M101", "ISBN102", 7, 0);

        List<LoanRecord> sorted = service.getSortedLoanHistory();

        System.out.println("=== SORTING TIE TEST ===");

        for (LoanRecord loan : sorted) {
            System.out.println(loan.getLoanId()
                    + " | Due: " + loan.getDueDate());
        }

        boolean sameDate = first.getDueDate()
                .equals(second.getDueDate());

        boolean correctOrder =
                sorted.get(0).getLoanId().equals("L001")
                && sorted.get(1).getLoanId().equals("L002");

        System.out.println("Same due date: " + sameDate);
        System.out.println("Loan ID order correct: " + correctOrder);
        System.out.println("TEST RESULT: "
                + (sameDate && correctOrder ? "PASS" : "FAIL"));
    }
}
