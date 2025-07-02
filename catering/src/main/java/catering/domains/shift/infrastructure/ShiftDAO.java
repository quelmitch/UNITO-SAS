package catering.domains.shift.infrastructure;

import catering.domains.shift.domain.Shift;
import catering.domains.staffmember.domain.StaffMember;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.persistence.PersistenceManager;
import catering.utils.LogManager;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.*;
import java.util.logging.Logger;

public class ShiftDAO {
    private static final Logger LOGGER = LogManager.getLogger(ShiftDAO.class);

    public static boolean save(Shift s) {
        if (s.getId() > 0) {
            return update(s);
        } else {
            Shift created = create(s.getDate(), s.getStartTime(), s.getEndTime());
            s.setId(created.getId());
            return true;
        }
    }

    public static boolean update(Shift s) {
        if (s.getId() <= 0) {
            return save(s);
        }
        String sql = "UPDATE Shifts SET date = ?, start_time = ?, end_time = ? WHERE id = ?";
        PersistenceManager.executeUpdate(sql, s.getDate(), s.getStartTime(), s.getEndTime(), s.getId());
        return true;
    }

    public static Shift loadById(int id) {
        String query = "SELECT * FROM Shifts WHERE id = ?";
        final Shift[] holder = new Shift[1];

        PersistenceManager.executeQuery(query, rs -> {
            Shift s = mapRowToShift(rs);
            s.getBookedStaffMembers().clear();
            s.getBookedStaffMembers().putAll(loadBookings(s));
            holder[0] = s;
        }, id);

        if (holder[0] == null) {
            LOGGER.warning("Shift with ID " + id + " not found");
        }
        return holder[0];
    }

    public static List<Shift> loadAll() {
        String query = "SELECT * FROM Shifts";
        List<Shift> shifts = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            Shift s = mapRowToShift(rs);
            s.getBookedStaffMembers().clear();
            s.getBookedStaffMembers().putAll(loadBookings(s));
            shifts.add(s);
        });

        shifts.sort(Comparator.comparing(Shift::getDate)
            .thenComparing(Shift::getStartTime));
        return shifts;
    }

    public static Shift create(Date date, Time start, Time end) {
        String sql = "INSERT INTO Shifts (date, start_time, end_time) VALUES (?, ?, ?)";
        PersistenceManager.executeUpdate(sql, date, start, end);
        int id = PersistenceManager.getLastId();
        LOGGER.info("Created new shift ID " + id + " on " + date);
        Shift s = new Shift(date, start, end);
        s.setId(id);
        return s;
    }

    public static void addBooking(Shift s, StaffMember u) {
        String sql = "INSERT INTO ShiftBookings (shift_id, staff_member_id) VALUES (?, ?)";
        PersistenceManager.executeUpdate(sql, s.getId(), u.getId());
        s.addBooking(u);
    }

    public static StaffMember removeBooking(Shift s, StaffMember u) {
        String sql = "DELETE FROM ShiftBookings WHERE shift_id = ? AND staff_member_id = ?";
        int rows = PersistenceManager.executeUpdate(sql, s.getId(), u.getId());
        if (rows > 0) {
            return s.removeBooking(u);
        }
        return null;
    }


    // HELPERS
    
    private static Shift mapRowToShift(ResultSet rs) throws SQLException {
        Shift s = new Shift();
        s.setId(rs.getInt("id"));
        try {
            s.setDate(Date.valueOf(rs.getString("date")));
            s.setStartTime(Time.valueOf(rs.getString("start_time")));
            s.setEndTime(Time.valueOf(rs.getString("end_time")));
        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Error parsing date/time for shift ID " + s.getId());
        }
        return s;
    }

    private static Map<Integer, StaffMember> loadBookings(Shift s) {
        Map<Integer, StaffMember> map = new HashMap<>();
        String sql = "SELECT staff_member_id FROM ShiftBookings WHERE shift_id = ?";
        PersistenceManager.executeQuery(sql, rs -> {
            int uid = rs.getInt("staff_member_id");
            StaffMember sm = StaffMemberDAO.loadById(uid);
            map.put(uid, sm);
        }, s.getId());
        LOGGER.fine("Loaded " + map.size() + " bookings for shift ID " + s.getId());
        return map;
    }
}
