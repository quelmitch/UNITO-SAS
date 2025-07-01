package catering.businesslogic.holidayleave;

import catering.businesslogic.holidayleave.HolidayLeave.RequestStatus;
import catering.businesslogic.staffmember.StaffMember;
import catering.businesslogic.staffmember.StaffMemberDAO;
import catering.persistence.PersistenceManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HolidayLeaveDAO {

    public static HolidayLeave loadById(int id) {
        HolidayLeave leave = new HolidayLeave();
        String query = "SELECT * FROM HolidayLeave WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> mapHolidayLeave(rs, leave), id);
        return leave.getId() > 0 ? leave : null;
    }

    public static List<HolidayLeave> loadAll() {
        List<HolidayLeave> leaves = new ArrayList<>();
        String query = "SELECT * FROM HolidayLeave";
        PersistenceManager.executeQuery(query, rs -> {
            HolidayLeave leave = new HolidayLeave();
            mapHolidayLeave(rs, leave);
            leaves.add(leave);
        });
        return leaves;
    }

    public static List<HolidayLeave> loadByStaffMember(StaffMember staff) {
        List<HolidayLeave> leaves = new ArrayList<>();
        String query = "SELECT * FROM HolidayLeave WHERE staff_member_id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            HolidayLeave leave = new HolidayLeave();
            mapHolidayLeave(rs, leave);
            leaves.add(leave);
        }, staff.getId());
        return leaves;
    }

    public static void save(HolidayLeave leave) {
        String query = "INSERT INTO HolidayLeave (staff_member_id, start_date, end_date, status) VALUES (?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query,
                leave.getStaffMember().getId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getStatus().name());

        leave.setId(PersistenceManager.getLastId());
    }

    public static boolean update(HolidayLeave leave) {
        String query = "UPDATE HolidayLeave SET start_date = ?, end_date = ?, status = ? WHERE id = ?";
        int updated = PersistenceManager.executeUpdate(query,
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getStatus().name(),
                leave.getId());
        return updated > 0;
    }

    public static boolean delete(HolidayLeave leave) {
        if (leave.getId() == 0) return false;
        int rows = PersistenceManager.executeUpdate("DELETE FROM HolidayLeave WHERE id = ?", leave.getId());
        if (rows > 0) {
            leave.setId(0);
            return true;
        }
        return false;
    }

    // Utility

    private static void mapHolidayLeave(ResultSet rs, HolidayLeave leave) throws SQLException {
        leave.setId(rs.getInt("id"));
        leave.setStartDate(rs.getDate("start_date"));
        leave.setEndDate(rs.getDate("end_date"));
        leave.setStatus(RequestStatus.valueOf(rs.getString("status")));
        StaffMember staff = StaffMemberDAO.loadById(rs.getInt("staff_member_id"));
        leave.setStaffMember(staff);
    }
}
