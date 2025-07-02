package catering.businesslogic.kitchen.domain;

import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.CatERing;
import catering.businesslogic.kitchen.infrastructure.AssignmentDAO;
import catering.businesslogic.kitchen.infrastructure.KitchenTaskDAO;
import catering.businesslogic.kitchen.infrastructure.KitchenTaskEventReceiver;
import catering.businesslogic.kitchen.infrastructure.SummarySheetDAO;
import catering.exceptions.SummarySheetException;
import catering.exceptions.UseCaseLogicException;
import catering.businesslogic.event.domain.Event;
import catering.businesslogic.event.domain.Service;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.staffmember.StaffMember;

public class KitchenTaskManager {

    private SummarySheet currentSumSheet;
    private final ArrayList<KitchenTaskEventReceiver> eventReceivers;

    public KitchenTaskManager() {
        eventReceivers = new ArrayList<>();
    }

    public void addEventReceiver(KitchenTaskEventReceiver rec) {
        this.eventReceivers.add(rec);
    }

    public SummarySheet generateSummarySheet(Event event, Service service) throws UseCaseLogicException {
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();

        if (!staffMember.hasRole(StaffMember.Role.CHEF))
            throw new UseCaseLogicException("Staff Member is not a chef");

        if (event == null)
            throw new UseCaseLogicException("Event not specified");

        if (service == null)
            throw new UseCaseLogicException("Service not specified");

        if (!event.containsService(service))
            throw new UseCaseLogicException("Event does not include service");

        if (!staffMember.equals(event.getChef()))
            throw new UseCaseLogicException("Staff Member not assigned chef");

        if (service.getMenu() == null)
            throw new UseCaseLogicException("Service lacks menu");

        SummarySheet newSummarySheet = new SummarySheet(service, staffMember);

        // Persist the new summary sheet
        SummarySheetDAO.save(newSummarySheet);

        this.setCurrentSumSheet(newSummarySheet);
        this.notifySheetGenerated(newSummarySheet);

        return newSummarySheet;
    }

    public List<SummarySheet> loadAllSumSheets() {
        return SummarySheetDAO.loadAll();
    }

    public SummarySheet openSumSheet(SummarySheet ss) throws UseCaseLogicException, SummarySheetException {
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        if (!staffMember.hasRole(StaffMember.Role.CHEF))
            throw new UseCaseLogicException();
        if (!ss.isOwner(staffMember))
            throw new SummarySheetException("StaffMember: " + staffMember.getEmail() + " is not owner of the SummarySheet");
        setCurrentSumSheet(ss);
        return ss;
    }

    public void addKitchenTask(KitchenTask t) {
        KitchenTask added = currentSumSheet.addTask(t);
        notifyTaskAdded(added);
    }

    public void moveTask(KitchenTask t, int pos) throws UseCaseLogicException {
        if (currentSumSheet == null || currentSumSheet.getTaskPosition(t) < 0)
            throw new UseCaseLogicException();
        if (pos < 0 || pos >= currentSumSheet.getTaskListSize())
            throw new IllegalArgumentException();
        this.currentSumSheet.moveTask(t, pos);

        // Persist new ordering
        SummarySheetDAO.updateTaskPositions(currentSumSheet);

        this.notifyTaskListSorted();
    }

    public void addTaskInformation(KitchenTask task, int quantity, int portions, long minutes)
        throws SummarySheetException, UseCaseLogicException {
        if (currentSumSheet == null)
            throw new UseCaseLogicException();
        if (currentSumSheet.getTaskPosition(task) < 0)
            throw new SummarySheetException("Task not found in this SummarySheet");
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity must be >= 0");
        if (portions < 0)
            throw new IllegalArgumentException("Portions must be >= 0");
        if (minutes < 0)
            throw new IllegalArgumentException("Minutes must be >= 0");

        KitchenTask t = currentSumSheet.addTaskInformation(task, quantity, portions, minutes);

        notifyTaskChanged(t);
    }

    public Assignment assignTask(KitchenTask t, Shift s) throws UseCaseLogicException {
        return assignTask(t, s, null);
    }

    public Assignment assignTask(KitchenTask t, Shift s, StaffMember cook) throws UseCaseLogicException {
        if (currentSumSheet == null) {
            throw new UseCaseLogicException("Cannot assign task because there is no active summary sheet.");
        }
        if (cook != null && !CatERing.getInstance().getShiftManager().isAvailable(cook, s)) {
            throw new UseCaseLogicException("Cook " + cook.getEmail() + " is not available for the selected shift.");
        }
        Assignment assignment = currentSumSheet.addAssignment(t, s, cook);

        // Persist assignment
        AssignmentDAO.save(currentSumSheet.getId(), assignment);

        this.notifyAssignmentAdded(assignment);
        return assignment;
    }

    public void modifyAssignment(Assignment ass) throws UseCaseLogicException, SummarySheetException {
        modifyAssignment(ass, ass.getShift(), null);
    }

    public void modifyAssignment(Assignment ass, StaffMember cook) throws UseCaseLogicException, SummarySheetException {
        modifyAssignment(ass, ass.getShift(), cook);
    }

    public void modifyAssignment(Assignment ass, Shift shift) throws UseCaseLogicException, SummarySheetException {
        modifyAssignment(ass, shift, null);
    }

    public void modifyAssignment(Assignment ass, Shift shift, StaffMember cook)
        throws UseCaseLogicException, SummarySheetException {
        if (currentSumSheet == null)
            throw new UseCaseLogicException();

        if (cook != null && !CatERing.getInstance().getShiftManager().isAvailable(cook, shift))
            throw new UseCaseLogicException();

        Assignment updated = currentSumSheet.modifyAssignment(ass, shift, cook);

        // Persist change
        AssignmentDAO.update(updated);

        notifyAssignmentChanged(updated);
    }

    public SummarySheet getCurrentSummarySheet() {
        return currentSumSheet;
    }

    public void setTaskReady(KitchenTask t) throws UseCaseLogicException {
        KitchenTask task = currentSumSheet.setTaskReady(t);

        // Persist task ready status if needed
        KitchenTaskDAO.update(task);

        notifyTaskChanged(task);
    }

    public void deleteAssignment(Assignment a) throws UseCaseLogicException {
        Assignment deleted = currentSumSheet.deleteAssignment(a);

        // Remove from DB
        AssignmentDAO.delete(deleted);

        notifyAssignmentDeleted(deleted);
    }

    private void setCurrentSumSheet(SummarySheet summarySheet) {
        currentSumSheet = summarySheet;
    }

    private void notifyTaskChanged(KitchenTask task) {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateTaskChanged(task);
        }
    }

    private void notifyAssignmentDeleted(Assignment ass) {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateAssignmentDeleted(ass);
        }
    }

    private void notifyAssignmentChanged(Assignment a) {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateAssignmentChanged(a);
        }
    }

    private void notifyAssignmentAdded(Assignment assignment) {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateAssignmentAdded(currentSumSheet, assignment);
        }
    }

    private void notifyTaskListSorted() {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateTaskListSorted(currentSumSheet);
        }
    }

    private void notifyTaskAdded(KitchenTask added) {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateTaskAdded(currentSumSheet, added);
        }
    }

    private void notifySheetGenerated(SummarySheet summarySheet) {
        for (KitchenTaskEventReceiver er : eventReceivers) {
            er.updateSheetGenerated(summarySheet);
        }
    }
}
