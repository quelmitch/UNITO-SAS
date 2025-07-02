package catering.domains.kitchen.infrastructure;

import catering.domains.kitchen.domain.Assignment;
import catering.domains.kitchen.domain.KitchenTask;
import catering.domains.kitchen.domain.SummarySheet;

import java.util.ArrayList;

public class KitchenTaskPublisher {

    private static final ArrayList<KitchenTaskSubscriber> subscribers = new ArrayList<>();

    public static void addSubscriber(KitchenTaskSubscriber subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public static void removeSubscriber(KitchenTaskSubscriber subscriber) {
        subscribers.remove(subscriber);
    }


    public static void notifyTaskChanged(KitchenTask task) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateTaskChanged(task);
        }
    }

    public static void notifyAssignmentDeleted(Assignment ass) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateAssignmentDeleted(ass);
        }
    }

    public static void notifyAssignmentChanged(Assignment a) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateAssignmentChanged(a);
        }
    }

    public static void notifyAssignmentAdded(Assignment assignment, SummarySheet summarySheet) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateAssignmentAdded(summarySheet, assignment);
        }
    }

    public static void notifyTaskListSorted(SummarySheet summarySheet) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateTaskListSorted(summarySheet);
        }
    }

    public static void notifyTaskAdded(KitchenTask added, SummarySheet summarySheet) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateTaskAdded(summarySheet, added);
        }
    }

    public static void notifySheetGenerated(SummarySheet summarySheet) {
        for (KitchenTaskSubscriber er : subscribers) {
            er.updateSheetGenerated(summarySheet);
        }
    }
}
