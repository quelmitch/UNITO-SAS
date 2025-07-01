package catering.businesslogic.staffmember;

import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import catering.exceptions.UseCaseLogicException;
import catering.util.LogManager;
import lombok.Data;

@Data
public class StaffMemberManager {
    private static Logger LOGGER = LogManager.getLogger(StaffMemberManager.class);

    private StaffMember currentStaffMember;

    public void fakeLogin(String email) throws UseCaseLogicException {
        this.currentStaffMember = StaffMemberDAO.loadByEmail(email);
    }

    public StaffMember addNewStaffMember(
        String email, String name, String surname, Date dateOfBirth, String address,
        String phone, int wage, StaffMember.EmploymentType employmentType
    ) throws UseCaseLogicException {
        isAuthorized();

        if (employmentType == StaffMember.EmploymentType.PERMANENTE &&
            !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("Only owner can add permanent staff");
        }

        StaffMember newStaff = new StaffMember(email, name, surname, dateOfBirth, address, phone, wage, employmentType);

        StaffMemberDAO.save(newStaff);
        StaffMemberEventNotifier.notifyCreated(newStaff);

        return newStaff;
    }

    public boolean deleteStaffMember(StaffMember staffMember) throws UseCaseLogicException {
        isAuthorized();

        boolean deleted = StaffMemberDAO.delete(staffMember);
        if (deleted) {
            StaffMemberEventNotifier.notifyDeleted(staffMember);
        }
        return deleted;
    }

    public boolean hasRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        isAuthorized();
        return staffMember.hasRole(role);
    }

    public boolean addRole(StaffMember staffMember, StaffMember.Role role, Set<String> jobs) throws UseCaseLogicException {
        isAuthorized();

        boolean added = staffMember.addRole(role, jobs);
        if (added) {
            StaffMemberDAO.update(staffMember);
            StaffMemberEventNotifier.notifyUpdated(staffMember);
        }
        return added;
    }

    public boolean removeRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        isAuthorized();

        boolean removed = staffMember.removeRole(role);
        if (removed) {
            StaffMemberDAO.update(staffMember);
            StaffMemberEventNotifier.notifyUpdated(staffMember);
        }
        return removed;
    }

    public boolean removeJobs(StaffMember staffMember, StaffMember.Role role, Set<String> jobs) throws UseCaseLogicException {
        isAuthorized();

        boolean removed = staffMember.removeJobs(role, jobs);
        if (removed) {
            StaffMemberDAO.update(staffMember);
            StaffMemberEventNotifier.notifyUpdated(staffMember);
        }
        return removed;
    }

    public void changeEmploymentType(StaffMember staffMember, StaffMember.EmploymentType employmentType) throws UseCaseLogicException {
        isAuthorized();

        if (employmentType == StaffMember.EmploymentType.PERMANENTE &&
            !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("Only owner can change to permanent");
        }

        staffMember.changeEmploymentType(employmentType);

        StaffMemberDAO.update(staffMember);
        StaffMemberEventNotifier.notifyUpdated(staffMember);
    }


    // HELPERS
    private void isAuthorized() throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE) && !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("User must be authorized");
        }
    }
}