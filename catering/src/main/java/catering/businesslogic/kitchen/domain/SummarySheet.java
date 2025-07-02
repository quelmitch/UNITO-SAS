package catering.businesslogic.kitchen.domain;

import catering.businesslogic.event.domain.Service;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.staffmember.StaffMember;
import catering.exceptions.SummarySheetException;
import catering.exceptions.UseCaseLogicException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for a summary sheet in the kitchen process.
 */
@Data
@NoArgsConstructor
public class SummarySheet {
    private int id;
    private Service service;
    private StaffMember owner;
    private List<KitchenTask> tasks;
    private List<Assignment> assignments;

    public SummarySheet(Service service, StaffMember owner) {
        this.service = service;
        this.owner = owner;
        this.tasks = new ArrayList<>();
        this.assignments = new ArrayList<>();

        service.getMenu().getKitchenProcesses()
            .forEach(process -> tasks.add(new KitchenTask(process, process.getName())));
    }

    public KitchenTask addTask(KitchenTask task) {
        this.tasks.add(task);
        return task;
    }

    public void moveTask(KitchenTask task, int newPosition) {
        tasks.remove(task);
        tasks.add(newPosition, task);
    }

    public Assignment addAssignment(KitchenTask task, Shift shift, StaffMember cook) {
        Assignment assignment = new Assignment(task, shift, cook);
        assignments.add(assignment);
        return assignment;
    }

    public Assignment modifyAssignment(Assignment ass, Shift shift, StaffMember cook) throws SummarySheetException {
        if (!assignments.contains(ass)) {
            throw new SummarySheetException("Assignment not part of this summary sheet");
        }
        ass.setShift(shift);
        ass.setCook(cook);
        return ass;
    }

    public Assignment deleteAssignment(Assignment ass) throws UseCaseLogicException {
        if (!assignments.remove(ass)) {
            throw new UseCaseLogicException("Assignment not found");
        }
        return ass;
    }

    public KitchenTask setTaskReady(KitchenTask task) throws UseCaseLogicException {
        if (!tasks.contains(task)) {
            throw new UseCaseLogicException("Task not in summary sheet");
        }
        task.setReady();
        return task;
    }

    public KitchenTask addTaskInformation(KitchenTask task, int quantity, int portions, long minutes) {
        task.setQuantity(quantity);
        task.setPortions(portions);
        return task;
    }

    public boolean isOwner(StaffMember user) {
        return owner.equals(user);
    }

    public int getTaskPosition(KitchenTask task) {
        return tasks.indexOf(task);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n\nSummarySheet [ID: ")
            .append(id)
            .append(", Owner: ").append(owner != null ? owner.getEmail() : "none");

        sb.append(", Service: ").append(service != null ? service.getName() : "none");
        sb.append(", Tasks: ").append(tasks != null ? tasks.size() : 0);
        sb.append(", Assignments: ").append(assignments != null ? assignments.size() : 0).append("]");

        if (tasks != null && !tasks.isEmpty()) {
            sb.append("\n\nTasks:");
            for (int i = 0; i < tasks.size(); i++) {
                sb.append("\n  ").append(i + 1).append(". ").append(tasks.get(i));
            }
        }

        if (assignments != null && !assignments.isEmpty()) {
            sb.append("\n\nAssignments:");
            for (int i = 0; i < assignments.size(); i++) {
                sb.append("\n  ").append(i + 1).append(". ").append(assignments.get(i));
            }
        }

        return sb.toString();
    }

    public int getTaskListSize() {
        return tasks.size();
    }
}
