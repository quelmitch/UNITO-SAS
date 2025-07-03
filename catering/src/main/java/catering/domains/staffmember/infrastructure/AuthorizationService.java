package catering.domains.staffmember.infrastructure;

import catering.app.CatERing;
import catering.domains.staffmember.domain.StaffMember;
import catering.exceptions.UseCaseLogicException;

public class AuthorizationService {
    public static void requireRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        if (!staffMember.hasRole(role)) {
            throw new UseCaseLogicException("User must have role: " + role);
        }
    }

    public static void requireAnyRole(StaffMember staffMember, StaffMember.Role... roles) throws UseCaseLogicException {
        for (StaffMember.Role role : roles) {
            if (staffMember.hasRole(role)) {
                return;
            }
        }
        throw new UseCaseLogicException("User must have one of the roles: " + java.util.Arrays.toString(roles));
    }

    public static void requireCurrentUserHasRole(StaffMember.Role role) throws UseCaseLogicException {
        StaffMember current = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        requireRole(current, role);
    }

    public static void requireCurrentUserHasAnyRole(StaffMember.Role... roles) throws UseCaseLogicException {
        StaffMember current = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        requireAnyRole(current, roles);
    }
}
