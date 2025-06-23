package catering.businesslogic.staffmember;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import catering.businesslogic.UseCaseLogicException;
import catering.util.LogManager;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class StaffMemberManager {
    private static final Logger LOGGER = LogManager.getLogger(StaffMemberManager.class);

    private StaffMember currentStaffMember;

    public void fakeLogin(String username) throws UseCaseLogicException {
        LOGGER.info("Attempting login for user: " + username);
        this.currentStaffMember = StaffMember.load(username);
        if (this.currentStaffMember == null) {
            LOGGER.warning("Login failed: user not found - " + username);
            throw new UseCaseLogicException("StaffMember not found");
        }
        LOGGER.info("StaffMember successfully logged in: " + username);
    }

    public StaffMember addNewStaffMember(String email, String name, String surname, Date dateOfBirth,
                                         String address, String phone, int wage, StaffMember.EmploymentType employmentType)
        throws UseCaseLogicException
    {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException();
        }

        if (
            employmentType == StaffMember.EmploymentType.PERMANENTE &&
            !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)
        ) {
            throw new UseCaseLogicException();
        }


        return new StaffMember(email, name, surname, dateOfBirth, address, phone, wage, employmentType);
    }

    public boolean hasRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException();
        }

        return staffMember.hasRole(role);
    }

    public boolean addRole(StaffMember staffMember, StaffMember.Role role, Set<String> jobs) throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException();
        }

        return staffMember.addRole(role, jobs);
    }

    public boolean removeRole(StaffMember staffMember, StaffMember.Role role) throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException();
        }

        return staffMember.removeRole(role);
    }

    public boolean removeJobs(StaffMember staffMember, StaffMember.Role role, Set<String> jobs) throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException();
        }

        return staffMember.removeJobs(role, jobs);
    }

    public void changeEmploymentType(StaffMember staffMember, StaffMember.EmploymentType employmentType) throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException();
        }

        if (
            employmentType == StaffMember.EmploymentType.PERMANENTE &&
            !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)
        ) {
            throw new UseCaseLogicException();
        }

        staffMember.changeEmploymentType(employmentType);
    }
}
