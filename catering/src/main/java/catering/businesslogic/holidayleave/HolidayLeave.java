package catering.businesslogic.holidayleave;

import catering.persistence.PersistenceManager;
import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
public class HolidayLeave {
    private int staffMemberId;
    private Date startDate;
    private Date endDate;
    boolean accepted = false;

    public HolidayLeave(int staffMemberId, Date startDate, Date endDate) {
        this.staffMemberId = staffMemberId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static List<HolidayLeave> load() {
        List<HolidayLeave> leaves = new ArrayList<>();

        String query = "SELECT * FROM HolidayLeave";

        PersistenceManager.executeQuery(query, rs -> {
            int staffMemberId = rs.getInt("staff_member_id");
            Date startDate = rs.getDate("start_date");
            Date endDate = rs.getDate("end_date");
            boolean accepted = rs.getBoolean("accepted");

            HolidayLeave leave = new HolidayLeave(staffMemberId, startDate, endDate);
            leave.setAccepted(accepted);
            leaves.add(leave);
        });

        return leaves;
    }

    public static List<HolidayLeave> loadByStaffMember(int uid) {
        List<HolidayLeave> leaves = new ArrayList<>();

        String query = "SELECT * FROM HolidayLeave WHERE staff_member_id = ?";

        PersistenceManager.executeQuery(query, rs -> {
            int staffMemberId = rs.getInt("staff_member_id");
            Date startDate = rs.getDate("start_date");
            Date endDate = rs.getDate("end_date");
            boolean accepted = rs.getBoolean("accepted");

            HolidayLeave leave = new HolidayLeave(staffMemberId, startDate, endDate);
            leave.setAccepted(accepted);
            leaves.add(leave);
        }, uid);

        return leaves;
    }
}
