package catering.domains.holidayleave.infrastructure;

import catering.domains.holidayleave.domain.HolidayLeave;

import java.util.ArrayList;
import java.util.List;

public class HolidayLeavePublisher {
    private static final List<HolidayLeaveSubscriber> receivers = new ArrayList<>();

    public static void registerReceiver(HolidayLeaveSubscriber receiver) {
        if (!receivers.contains(receiver)) {
            receivers.add(receiver);
        }
    }

    public static void notifyHolidayLeaveCreated(HolidayLeave leave) {
        for (HolidayLeaveSubscriber receiver : receivers) {
            receiver.updateHolidayLeaveCreated(leave);
        }
    }

    public static void notifyHolidayLeaveUpdated(HolidayLeave leave) {
        for (HolidayLeaveSubscriber receiver : receivers) {
            receiver.updateHolidayLeaveUpdated(leave);
        }
    }

    public static void notifyHolidayLeaveDeleted(HolidayLeave leave) {
        for (HolidayLeaveSubscriber receiver : receivers) {
            receiver.updateHolidayLeaveDeleted(leave);
        }
    }
}
