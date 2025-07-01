package catering.businesslogic.holidayleave;

public interface HolidayLeaveEventReceiver {
    void updateHolidayLeaveCreated(HolidayLeave leave);
    void updateHolidayLeaveDeleted(HolidayLeave leave);
    void updateHolidayLeaveUpdated(HolidayLeave leave);
}
