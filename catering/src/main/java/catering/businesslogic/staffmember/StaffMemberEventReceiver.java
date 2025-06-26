package catering.businesslogic.staffmember;

public interface StaffMemberEventReceiver {
    void updateStaffMemberCreated(StaffMember s);
    void updateStaffMemberDeleted(StaffMember s);
    void updateStaffMemberUpdated(StaffMember s);
}
