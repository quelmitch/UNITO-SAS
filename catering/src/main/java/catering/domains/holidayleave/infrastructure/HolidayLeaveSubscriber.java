package catering.domains.holidayleave.infrastructure;

import catering.domains.holidayleave.domain.HolidayLeave;

public interface HolidayLeaveSubscriber {
    void updateHolidayLeaveCreated(HolidayLeave leave);
    void updateHolidayLeaveDeleted(HolidayLeave leave);
    void updateHolidayLeaveUpdated(HolidayLeave leave);
}
