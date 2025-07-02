package catering.domains.staffmember.domain;

import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.domains.staffmember.infrastructure.StaffMemberPublisher;
import catering.exceptions.UseCaseLogicException;
import catering.utils.LogManager;
import lombok.Data;

@Data
public class StaffMemberManager {
    private static Logger LOGGER = LogManager.getLogger(StaffMemberManager.class);

    private StaffMember currentStaffMember;

    public void fakeLogin(String email) throws UseCaseLogicException {
        this.currentStaffMember = StaffMemberDAO.loadByEmail(email);
    }

    public void isAdministrator(StaffMember staffMember) throws UseCaseLogicException {
        if (!staffMember.hasRole(StaffMember.Role.ORGANIZZATORE) && !staffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("User must be authorized");
        }
    }


    public StaffMember addNewStaffMember(
        String email, String name, String surname, Date dateOfBirth, String address,
        String phone, int wage, StaffMember.EmploymentType employmentType
    ) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);

        if (employmentType == StaffMember.EmploymentType.PERMANENTE &&
            !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("Only owner can add permanent staff");
        }

        StaffMember newStaff = new StaffMember(email, name, surname, dateOfBirth, address, phone, wage, employmentType);

        StaffMemberDAO.save(newStaff);
        StaffMemberPublisher.notifyCreated(newStaff);

        return newStaff;
    }

    public boolean deleteStaffMember(StaffMember staffMember) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);

        boolean deleted = StaffMemberDAO.delete(staffMember);
        if (deleted) {
            StaffMemberPublisher.notifyDeleted(staffMember);
        }
        return deleted;
    }


    // ROLES

    public boolean hasRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);
        return staffMember.hasRole(role);
    }

    public boolean addRole(StaffMember staffMember, StaffMember.Role role, Set<String> jobs) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);

        boolean added = staffMember.addRole(role, jobs);
        if (added) {
            StaffMemberDAO.update(staffMember);
            StaffMemberPublisher.notifyUpdated(staffMember);
        }
        return added;
    }

    public boolean removeRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);

        boolean removed = staffMember.removeRole(role);
        if (removed) {
            StaffMemberDAO.update(staffMember);
            StaffMemberPublisher.notifyUpdated(staffMember);
        }
        return removed;
    }

    public boolean removeJobs(StaffMember staffMember, StaffMember.Role role, Set<String> jobs) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);

        boolean removed = staffMember.removeJobs(role, jobs);
        if (removed) {
            StaffMemberDAO.update(staffMember);
            StaffMemberPublisher.notifyUpdated(staffMember);
        }
        return removed;
    }


    // EMPLOYMENT TYPE

    public void changeEmploymentType(StaffMember staffMember, StaffMember.EmploymentType employmentType) throws UseCaseLogicException {
        isAdministrator(currentStaffMember);

        if (employmentType == StaffMember.EmploymentType.PERMANENTE &&
            !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("Only owner can change to permanent");
        }

        staffMember.changeEmploymentType(employmentType);

        StaffMemberDAO.update(staffMember);
        StaffMemberPublisher.notifyUpdated(staffMember);
    }
}