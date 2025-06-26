package catering.businesslogic.staffmember;

public class StaffMemberPersistence implements StaffMemberEventReceiver {

    @Override
    public void updateStaffMemberCreated(StaffMember s) {
        StaffMemberDAO.save(s);
    }

    @Override
    public void updateStaffMemberDeleted(StaffMember s) {
        StaffMemberDAO.delete(s);
    }

    @Override
    public void updateStaffMemberUpdated(StaffMember s) {
        StaffMemberDAO.update(s);
    }
}

