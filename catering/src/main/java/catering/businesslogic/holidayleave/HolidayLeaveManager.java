package catering.businesslogic.holidayleave;

import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staffmember.StaffMember;
import catering.businesslogic.staffmember.StaffMemberDAO;
import catering.util.DateUtils;
import catering.util.LogManager;
import lombok.Data;

import java.util.Date;
import java.util.logging.Logger;

@Data
public class HolidayLeaveManager {
    private static final Logger LOGGER = LogManager.getLogger(HolidayLeaveManager.class);

    private StaffMember currentStaffMember;

    private void isAuthorized() throws UseCaseLogicException {
        if (!currentStaffMember.hasRole(StaffMember.Role.ORGANIZZATORE) &&
                !currentStaffMember.hasRole(StaffMember.Role.PROPRIETARIO)) {
            throw new UseCaseLogicException("User must be authorized");
        }
    }

    public void fakeLogin(String email) throws UseCaseLogicException {
        this.currentStaffMember = StaffMemberDAO.loadByEmail(email);
        if (this.currentStaffMember == null) {
            throw new UseCaseLogicException("User not found");
        }
    }

    // Request a new holiday leave
    public HolidayLeave requestHolidayLeave(Date startDate, Date endDate) throws UseCaseLogicException {
        if (startDate == null || endDate == null || !endDate.after(startDate)) {
            throw new UseCaseLogicException("Invalid date range");
        }

        HolidayLeave leave = new HolidayLeave(currentStaffMember, DateUtils.safeValueOf(String.valueOf(startDate)), DateUtils.safeValueOf(String.valueOf(endDate)), HolidayLeave.RequestStatus.IN_ATTESA);
        HolidayLeaveEventNotifier.notifyHolidayLeaveCreated(leave);

        return leave;
    }

    // Approve a holiday leave
    public void approveLeave(HolidayLeave leave) throws UseCaseLogicException {
        isAuthorized();

        leave.setStatus(HolidayLeave.RequestStatus.ACCETTATA);
        HolidayLeaveEventNotifier.notifyHolidayLeaveUpdated(leave);
    }

    // Reject a holiday leave
    public void rejectLeave(HolidayLeave leave) throws UseCaseLogicException {
        isAuthorized();

        leave.setStatus(HolidayLeave.RequestStatus.RIFIUTATA);
        HolidayLeaveEventNotifier.notifyHolidayLeaveUpdated(leave);
    }

    // Cancel a pending request by the same staff member
    public boolean cancelLeave(HolidayLeave leave) throws UseCaseLogicException {
        if (!leave.getStaffMember().equals(currentStaffMember)) {
            throw new UseCaseLogicException("You can only cancel your own leaves");
        }

        if (leave.getStatus() != HolidayLeave.RequestStatus.IN_ATTESA) {
            throw new UseCaseLogicException("Only pending requests can be canceled");
        }

        boolean deleted = HolidayLeaveDAO.delete(leave);
        if (deleted) {
            HolidayLeaveEventNotifier.notifyHolidayLeaveDeleted(leave);
        }

        return deleted;
    }
}
