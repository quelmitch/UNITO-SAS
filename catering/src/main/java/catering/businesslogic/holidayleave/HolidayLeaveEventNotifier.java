package catering.businesslogic.holidayleave;

import java.util.ArrayList;
import java.util.List;

public class HolidayLeaveEventNotifier {
    private static final List<HolidayLeaveEventReceiver> receivers = new ArrayList<>();

    public static void registerReceiver(HolidayLeaveEventReceiver receiver) {
        if (!receivers.contains(receiver)) {
            receivers.add(receiver);
        }
    }

    public static void notifyHolidayLeaveCreated(HolidayLeave leave) {
        for (HolidayLeaveEventReceiver receiver : receivers) {
            receiver.updateHolidayLeaveCreated(leave);
        }
    }

    public static void notifyHolidayLeaveUpdated(HolidayLeave leave) {
        for (HolidayLeaveEventReceiver receiver : receivers) {
            receiver.updateHolidayLeaveUpdated(leave);
        }
    }

    public static void notifyHolidayLeaveDeleted(HolidayLeave leave) {
        for (HolidayLeaveEventReceiver receiver : receivers) {
            receiver.updateHolidayLeaveDeleted(leave);
        }
    }
}
