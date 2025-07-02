package catering.businesslogic.shift;

import catering.businesslogic.staffmember.StaffMember;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import catering.util.LogManager;

@Data
public class Shift {
    private static final Logger LOGGER = LogManager.getLogger(Shift.class);

    private int id;
    private Date date;
    private Time startTime;
    private Time endTime;
    private Map<Integer, StaffMember> bookedStaffMembers = new HashMap<>();

    public Shift() { }

    public Shift(Date date, Time startTime, Time endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void addBooking(StaffMember u) {
        if (bookedStaffMembers.containsKey(u.getId())) {
            LOGGER.warning("StaffMember " + u.getEmail() + " is already booked for this shift");
            return;
        }
        bookedStaffMembers.put(u.getId(), u);
        LOGGER.info("Added booking for user " + u.getEmail() + " to shift ID " + id);
    }

    public StaffMember removeBooking(StaffMember u) {
        if (!bookedStaffMembers.containsKey(u.getId())) {
            LOGGER.warning("StaffMember " + u.getEmail() + " is not booked for this shift");
            return null;
        }
        StaffMember removed = bookedStaffMembers.remove(u.getId());
        LOGGER.info("Removed booking for user " + u.getEmail() + " from shift ID " + id);
        return removed;
    }

    public boolean isBooked(StaffMember u) {
        return bookedStaffMembers.containsKey(u.getId());
    }

    public Map<Integer, StaffMember> getBookedStaffMembers() {
        return new HashMap<>(bookedStaffMembers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(date)
            .append(" | <")
            .append(startTime)
            .append(" - ")
            .append(endTime)
            .append(">");
        bookedStaffMembers.values().forEach(u -> sb.append("\n\t - ").append(u));
        return sb.toString();
    }
}
