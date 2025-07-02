package catering.domains.staffmember.infrastructure;

import catering.domains.staffmember.domain.StaffMember;

public interface StaffMemberSubscriber {
    void updateStaffMemberCreated(StaffMember s);
    void updateStaffMemberDeleted(StaffMember s);
    void updateStaffMemberUpdated(StaffMember s);
}
