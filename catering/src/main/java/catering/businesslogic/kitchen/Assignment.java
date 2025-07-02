package catering.businesslogic.kitchen;

import catering.businesslogic.shift.Shift;
import catering.businesslogic.staffmember.StaffMember;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a task assignment to a shift and optionally a cook
 */
@Data
@NoArgsConstructor
public class Assignment {
    private int id;
    private Shift shift;
    private KitchenTask task;
    private StaffMember cook;

    public Assignment(KitchenTask task, Shift shift, StaffMember cook) {
        this.task = task;
        this.shift = shift;
        this.cook = cook;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task: ").append(task != null ? task.getDescription() : "none");
        sb.append(", Cook: ").append(cook != null ? cook.getEmail() : "unassigned");

        if (shift != null) {
            sb.append(", Shift: ").append(shift.getDate())
                .append(" (").append(shift.getStartTime())
                .append("-").append(shift.getEndTime()).append(")");
        }

        return sb.toString();
    }
}
