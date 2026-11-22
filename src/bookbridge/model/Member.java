
package bookbridge.model;

public abstract class Member {

    private final String memberId;
    private final String memberName;
    private final MembershipType membershipType;

    public Member(String memberId, String memberName,
                  MembershipType membershipType) {

        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Member ID cannot be blank.");
        }

        if (memberName == null || memberName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Member name cannot be blank.");
        }

        if (membershipType == null) {
            throw new IllegalArgumentException(
                    "Membership type is required.");
        }

        this.memberId = memberId.trim();
        this.memberName = memberName.trim();
        this.membershipType = membershipType;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public int getMaximumLoanDays() {
        return membershipType.getMaximumLoanDays();
    }

    public int getDailyFine() {
        return membershipType.getDailyFine();
    }

    public int calculateLateCharge(int lateDays) {
        if (lateDays < 0) {
            throw new IllegalArgumentException(
                    "Late days cannot be negative.");
        }

        return lateDays * getDailyFine();
    }
}
