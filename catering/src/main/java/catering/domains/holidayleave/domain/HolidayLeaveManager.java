package catering.domains.holidayleave.domain;

import catering.app.CatERing;
import catering.domains.holidayleave.infrastructure.HolidayLeaveDAO;
import catering.domains.holidayleave.infrastructure.HolidayLeaveEventNotifier;
import catering.exceptions.UseCaseLogicException;
import catering.domains.staffmember.domain.StaffMember;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.utils.DateUtils;
import catering.utils.LogManager;
import lombok.Data;

import java.util.Date;
import java.util.logging.Logger;

@Data
public class HolidayLeaveManager {
    private final Logger Logger = LogManager.getLogger(HolidayLeaveManager.class);

    // Request a new holiday leave
    public HolidayLeave requestHolidayLeave(Date startDate, Date endDate) throws UseCaseLogicException {
        if (startDate == null || endDate == null || !endDate.after(startDate)) {
            throw new UseCaseLogicException("Invalid date range");
        }

        HolidayLeave leave = new HolidayLeave(
            CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember(),
            DateUtils.safeValueOf(String.valueOf(startDate)),
            DateUtils.safeValueOf(String.valueOf(endDate)),
            HolidayLeave.RequestStatus.IN_ATTESA
        );
        HolidayLeaveEventNotifier.notifyHolidayLeaveCreated(leave);

        return leave;
    }

    // Approve a holiday leave
    public void approveLeave(HolidayLeave leave) throws UseCaseLogicException {
        StaffMember currentStaffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        CatERing.getInstance().getStaffMemberManager().isAdministrator(currentStaffMember);

        leave.setStatus(HolidayLeave.RequestStatus.ACCETTATA);
        HolidayLeaveEventNotifier.notifyHolidayLeaveUpdated(leave);
    }

    // Reject a holiday leave
    public void rejectLeave(HolidayLeave leave) throws UseCaseLogicException {
        StaffMember currentStaffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        CatERing.getInstance().getStaffMemberManager().isAdministrator(currentStaffMember);

        leave.setStatus(HolidayLeave.RequestStatus.RIFIUTATA);
        HolidayLeaveEventNotifier.notifyHolidayLeaveUpdated(leave);
    }

    // Cancel a pending request by the same staff member
    public boolean cancelLeave(HolidayLeave leave) throws UseCaseLogicException {
        StaffMember currentStaffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();

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
