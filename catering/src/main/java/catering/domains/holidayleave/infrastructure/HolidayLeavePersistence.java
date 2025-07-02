package catering.domains.holidayleave.infrastructure;

import catering.domains.holidayleave.domain.HolidayLeave;

public class HolidayLeavePersistence implements HolidayLeaveEventReceiver {

    @Override
    public void updateHolidayLeaveCreated(HolidayLeave leave) {
        HolidayLeaveDAO.save(leave);
    }

    @Override
    public void updateHolidayLeaveDeleted(HolidayLeave leave) {
        HolidayLeaveDAO.delete(leave);
    }

    @Override
    public void updateHolidayLeaveUpdated(HolidayLeave leave) {
        HolidayLeaveDAO.update(leave);
    }
}
