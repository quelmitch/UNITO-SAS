package catering.domains.shift.domain;

import catering.domains.shift.infrastructure.ShiftDAO;
import catering.domains.staffmember.domain.StaffMember;
import catering.utils.LogManager;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages shift operations in the CatERing system.
 * Acts as a facade to the Shift class, handling shift creation, retrieval, and
 * booking.
 */
@NoArgsConstructor
public class ShiftManager {
    private final static Logger LOGGER = LogManager.getLogger(ShiftManager.class);

    public List<Shift> getShiftTable() {
        return ShiftDAO.loadAll();
    }

    public boolean isAvailable(StaffMember u, Shift s) {
        return s.isBooked(u);
    }

    public Shift createShift(Date date, Time startTime, Time endTime, String workPlace, boolean isKitchen) {
        LOGGER.info("Creating new shift on " + date + " at " + workPlace);
        return ShiftDAO.create(date, startTime, endTime);
    }

    public Shift loadShiftById(int id) {
        LOGGER.info("Loading shift with ID: " + id);
        return ShiftDAO.loadById(id);
    }

    public void updateShift(Shift shift) {
        LOGGER.info("Updating shift with ID: " + shift.getId());
        ShiftDAO.update(shift);
    }

    public void bookStaffMemberForShift(Shift shift, StaffMember staffMember) {
        if (isAvailable(staffMember, shift)) {
            LOGGER.info("Booking user " + staffMember.getEmail() + " for shift ID: " + shift.getId());
            shift.addBooking(staffMember);
        } else {
            LOGGER.warning("StaffMember " + staffMember.getEmail() + " is already booked for shift ID: " + shift.getId());
        }
    }

    public StaffMember removeStaffMemberFromShift(Shift shift, StaffMember staffMember) {
        LOGGER.info("Removing user " + staffMember.getEmail() + " from shift ID: " + shift.getId());
        return ShiftDAO.removeBooking(shift, staffMember);
    }

    public Map<Integer, StaffMember> getBookedStaffMembers(Shift shift) {
        return shift.getBookedStaffMembers();
    }

    public List<Shift> getShiftsForDate(Date date) {
        List<Shift> dateShifts = new ArrayList<>();
        for (Shift shift : getShiftTable()) {
            if (shift.getDate().equals(date)) {
                dateShifts.add(shift);
            }
        }
        return dateShifts;
    }
}
