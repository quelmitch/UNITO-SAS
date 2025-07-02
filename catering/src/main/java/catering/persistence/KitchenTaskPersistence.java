package catering.persistence;

import catering.businesslogic.kitchen.*;

public class KitchenTaskPersistence implements KitchenTaskEventReceiver {

    @Override
    public void updateSheetGenerated(SummarySheet summarySheet) {
        SummarySheetDAO.save(summarySheet);
    }

    @Override
    public void updateTaskAdded(SummarySheet currentSumSheet, KitchenTask added) {
        KitchenTaskDAO.saveNewTask(currentSumSheet.getId(), added, currentSumSheet.getTaskPosition(added));
    }

    @Override
    public void updateTaskListSorted(SummarySheet currentSumSheet) {
        SummarySheetDAO.updateTaskPositions(currentSumSheet);
    }

    @Override
    public void updateAssignmentAdded(SummarySheet currentSumSheet, Assignment a) {
        AssignmentDAO.save(currentSumSheet.getId(), a);
    }

    @Override
    public void updateAssignmentChanged(Assignment a) {
        AssignmentDAO.update(a);
    }

    @Override
    public void updateAssignmentDeleted(Assignment ass) {
        AssignmentDAO.delete(ass);
    }

    @Override
    public void updateTaskChanged(KitchenTask task) {
        KitchenTaskDAO.update(task);
    }

}