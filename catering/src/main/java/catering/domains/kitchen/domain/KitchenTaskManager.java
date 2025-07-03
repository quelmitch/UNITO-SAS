package catering.domains.kitchen.domain;

import java.util.List;

import catering.app.CatERing;
import catering.domains.kitchen.infrastructure.*;
import catering.domains.staffmember.infrastructure.AuthorizationService;
import catering.exceptions.SummarySheetException;
import catering.exceptions.UseCaseLogicException;
import catering.domains.event.domain.Event;
import catering.domains.event.domain.Service;
import catering.domains.shift.domain.Shift;
import catering.domains.staffmember.domain.StaffMember;
import lombok.Data;

@Data
public class KitchenTaskManager {
    private SummarySheet currentSummarySheet;


    // SUMMARY SHEET

    public SummarySheet generateSummarySheet(Event event, Service service) throws UseCaseLogicException {
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();

        AuthorizationService.requireCurrentUserHasRole(StaffMember.Role.CHEF);

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
        this.setCurrentSummarySheet(newSummarySheet);

        SummarySheetDAO.save(newSummarySheet);
        KitchenTaskPublisher.notifySheetGenerated(newSummarySheet);

        return newSummarySheet;
    }

    public List<SummarySheet> loadAllSumSheets() {
        return SummarySheetDAO.loadAll();
    }

    public SummarySheet openSumSheet(SummarySheet ss) throws UseCaseLogicException, SummarySheetException {
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        AuthorizationService.requireCurrentUserHasRole(StaffMember.Role.CHEF);

        if (!ss.isOwner(staffMember))
            throw new SummarySheetException("StaffMember: " + staffMember.getEmail() + " is not owner of the SummarySheet");
        setCurrentSummarySheet(ss);
        return ss;
    }


    // TASKS

    public void addKitchenTask(KitchenTask t) {
        KitchenTask kitchenTask = currentSummarySheet.addTask(t);

        KitchenTaskDAO.saveNewTask(currentSummarySheet.getId(), kitchenTask, currentSummarySheet.getTaskPosition(kitchenTask));
        KitchenTaskPublisher.notifyTaskAdded(kitchenTask, currentSummarySheet);
    }

    public void moveTask(KitchenTask t, int pos) throws UseCaseLogicException {
        if (currentSummarySheet == null || currentSummarySheet.getTaskPosition(t) < 0)
            throw new UseCaseLogicException();
        if (pos < 0 || pos >= currentSummarySheet.getTaskListSize())
            throw new IllegalArgumentException();
        this.currentSummarySheet.moveTask(t, pos);

        SummarySheetDAO.updateTaskPositions(currentSummarySheet);
        KitchenTaskPublisher.notifyTaskListSorted(currentSummarySheet);
    }

    public void addTaskInformation(KitchenTask task, int quantity, int portions, long minutes)
        throws SummarySheetException, UseCaseLogicException {
        if (currentSummarySheet == null)
            throw new UseCaseLogicException();
        if (currentSummarySheet.getTaskPosition(task) < 0)
            throw new SummarySheetException("Task not found in this SummarySheet");
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity must be >= 0");
        if (portions < 0)
            throw new IllegalArgumentException("Portions must be >= 0");
        if (minutes < 0)
            throw new IllegalArgumentException("Minutes must be >= 0");

        KitchenTask t = currentSummarySheet.addTaskInformation(task, quantity, portions, minutes);

        KitchenTaskDAO.update(task);
        KitchenTaskPublisher.notifyTaskChanged(t);
    }

    public Assignment assignTask(KitchenTask t, Shift s) throws UseCaseLogicException {
        return assignTask(t, s, null);
    }

    public Assignment assignTask(KitchenTask t, Shift s, StaffMember cook) throws UseCaseLogicException {
        if (currentSummarySheet == null) {
            throw new UseCaseLogicException("Cannot assign task because there is no active summary sheet.");
        }
        if (cook != null && !CatERing.getInstance().getShiftManager().isAvailable(cook, s)) {
            throw new UseCaseLogicException("Cook " + cook.getEmail() + " is not available for the selected shift.");
        }

        Assignment assignment = currentSummarySheet.addAssignment(t, s, cook);

        AssignmentDAO.save(currentSummarySheet.getId(), assignment);
        KitchenTaskPublisher.notifyAssignmentAdded(assignment, currentSummarySheet);

        return assignment;
    }

    public void setTaskReady(KitchenTask t) throws UseCaseLogicException {
        KitchenTask task = currentSummarySheet.setTaskReady(t);

        KitchenTaskDAO.update(task);
        KitchenTaskPublisher.notifyTaskChanged(task);
    }


    // ASSIGNMENTS

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
        if (currentSummarySheet == null)
            throw new UseCaseLogicException();

        if (cook != null && !CatERing.getInstance().getShiftManager().isAvailable(cook, shift))
            throw new UseCaseLogicException();

        Assignment updated = currentSummarySheet.modifyAssignment(ass, shift, cook);

        AssignmentDAO.update(updated);
        KitchenTaskPublisher.notifyAssignmentChanged(updated);
    }

    public void deleteAssignment(Assignment a) throws UseCaseLogicException {
        Assignment deleted = currentSummarySheet.deleteAssignment(a);

        AssignmentDAO.delete(deleted);
        KitchenTaskPublisher.notifyAssignmentDeleted(deleted);
    }

}
