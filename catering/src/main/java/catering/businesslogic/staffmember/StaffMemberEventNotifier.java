package catering.businesslogic.staffmember;

import java.util.ArrayList;
import java.util.List;

public class StaffMemberEventNotifier {

    private static final List<StaffMemberEventReceiver> receivers = new ArrayList<>();

    public static void registerReceiver(StaffMemberEventReceiver receiver) {
        receivers.add(receiver);
    }

    public static void notifyCreated(StaffMember staffMember) {
        for (StaffMemberEventReceiver receiver : receivers) {
            receiver.updateStaffMemberCreated(staffMember);
        }
    }

    public static void notifyUpdated(StaffMember staffMember) {
        for (StaffMemberEventReceiver receiver : receivers) {
            receiver.updateStaffMemberUpdated(staffMember);
        }
    }

    public static void notifyDeleted(StaffMember staffMember) {
        for (StaffMemberEventReceiver receiver : receivers) {
            receiver.updateStaffMemberDeleted(staffMember);
        }
    }
}
