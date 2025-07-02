package catering.businesslogic.kitchen.infrastructure;

import catering.businesslogic.kitchen.domain.Assignment;
import catering.businesslogic.kitchen.domain.KitchenTask;
import catering.businesslogic.kitchen.domain.SummarySheet;

public interface KitchenTaskEventReceiver {

    void updateSheetGenerated(SummarySheet summarySheet);

    void updateTaskAdded(SummarySheet currentSumSheet, KitchenTask added);

    void updateTaskListSorted(SummarySheet currentSumSheet);

    void updateAssignmentAdded(SummarySheet currentSumSheet, Assignment a);

    void updateAssignmentChanged(Assignment a);

    void updateAssignmentDeleted(Assignment ass);

    void updateTaskChanged(KitchenTask task);

}