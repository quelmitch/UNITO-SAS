package catering.domains.staffmember.infrastructure;

import catering.domains.staffmember.domain.StaffMember;

import java.util.ArrayList;
import java.util.List;

public class StaffMemberPublisher {

    private static final List<StaffMemberSubscriber> subscribers = new ArrayList<>();

    public static void registerSubscriber(StaffMemberSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public static void removeSubscriber(StaffMemberSubscriber subscriber) {
        subscribers.remove(subscriber);
    }


    public static void notifyCreated(StaffMember staffMember) {
        for (StaffMemberSubscriber receiver : subscribers) {
            receiver.updateStaffMemberCreated(staffMember);
        }
    }

    public static void notifyUpdated(StaffMember staffMember) {
        for (StaffMemberSubscriber receiver : subscribers) {
            receiver.updateStaffMemberUpdated(staffMember);
        }
    }

    public static void notifyDeleted(StaffMember staffMember) {
        for (StaffMemberSubscriber receiver : subscribers) {
            receiver.updateStaffMemberDeleted(staffMember);
        }
    }
}
