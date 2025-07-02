package catering.domains.kitchen.infrastructure;

import catering.domains.kitchen.domain.Assignment;
import catering.domains.kitchen.domain.KitchenTask;
import catering.domains.kitchen.domain.SummarySheet;

public interface KitchenTaskSubscriber {

    void updateSheetGenerated(SummarySheet summarySheet);

    void updateTaskAdded(SummarySheet currentSumSheet, KitchenTask added);

    void updateTaskListSorted(SummarySheet currentSumSheet);

    void updateAssignmentAdded(SummarySheet currentSumSheet, Assignment a);

    void updateAssignmentChanged(Assignment a);

    void updateAssignmentDeleted(Assignment ass);

    void updateTaskChanged(KitchenTask task);

}