package catering.businesslogic.staffmember;

import catering.businesslogic.staffmember.StaffMember;
import catering.businesslogic.staffmember.StaffMember.EmploymentType;
import catering.businesslogic.staffmember.StaffMember.Role;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StaffMemberDAO {

    public static StaffMember loadById(int id) {
        StaffMember staff = new StaffMember();
        String query = "SELECT * FROM StaffMembers WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> mapStaffMember(rs, staff), id);

        if (staff.getId() > 0) loadRolesForStaffMember(staff);
        return staff;
    }

    public static StaffMember loadByEmail(String email) {
        StaffMember staff = new StaffMember();
        String query = "SELECT * FROM StaffMembers WHERE email = ?";
        PersistenceManager.executeQuery(query, rs -> mapStaffMember(rs, staff), email);

        if (staff.getId() > 0) {
            loadRolesForStaffMember(staff);
            return staff;
        }
        return null;
    }

    public static List<StaffMember> loadAll() {
        List<StaffMember> staffList = new ArrayList<>();
        String query = "SELECT * FROM StaffMembers";

        PersistenceManager.executeQuery(query, rs -> {
            StaffMember staff = new StaffMember();
            mapStaffMember(rs, staff);
            loadRolesForStaffMember(staff);
            staffList.add(staff);
        });

        return staffList;
    }

    public static void save(StaffMember staff) {
        String query = "INSERT INTO StaffMembers (email, name, surname, dateOfBirth, address, phone, wage, employmentType_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PersistenceManager.executeUpdate(query,
                staff.getEmail(),
                staff.getName(),
                staff.getSurname(),
                staff.getDateOfBirth(),
                staff.getAddress(),
                staff.getPhone(),
                staff.getWage(),
                staff.getEmploymentType() == EmploymentType.PERMANENTE ? 0 : 1);

        staff.setId(PersistenceManager.getLastId());
        saveRoles(staff);
    }

    public static boolean update(StaffMember staff) {
        String query = "UPDATE StaffMembers SET email = ?, name = ?, surname = ?, dateOfBirth = ?, address = ?, phone = ?, wage = ?, employmentType_id = ? WHERE id = ?";

        int updated = PersistenceManager.executeUpdate(query,
                staff.getEmail(),
                staff.getName(),
                staff.getSurname(),
                staff.getDateOfBirth(),
                staff.getAddress(),
                staff.getPhone(),
                staff.getWage(),
                staff.getEmploymentType() == EmploymentType.PERMANENTE ? 0 : 1,
                staff.getId());

        saveRoles(staff);
        return updated > 0;
    }

    public static boolean delete(StaffMember staff) {
        int id = staff.getId();
        if (id == 0) return false;

        PersistenceManager.executeUpdate("DELETE FROM StaffMemberRoles WHERE staff_member_id = ?", id);
        int rows = PersistenceManager.executeUpdate("DELETE FROM StaffMembers WHERE id = ?", id);

        if (rows > 0) {
            staff.setId(0);
            return true;
        }
        return false;
    }

    // ----- Helper Methods -----

    private static void mapStaffMember(ResultSet rs, StaffMember staff) throws SQLException {
        staff.setId(rs.getInt("id"));
        staff.setEmail(rs.getString("email"));
        staff.setName(rs.getString("name"));
        staff.setSurname(rs.getString("surname"));
        staff.setDateOfBirth(DateUtils.getDateFromResultSet(rs, "dateOfBirth"));
        staff.setAddress(rs.getString("address"));
        staff.setPhone(rs.getString("phone"));
        staff.setWage(rs.getInt("wage"));
        staff.setEmploymentType(rs.getInt("employmentType_id") == 0 ? EmploymentType.PERMANENTE : EmploymentType.OCCASIONALE);
    }

    private static void loadRolesForStaffMember(StaffMember staff) {
        String query = "SELECT * FROM StaffMemberRoles WHERE staff_member_id = ?";

        PersistenceManager.executeQuery(query, rs -> {
            Role role = Role.values()[rs.getInt("role_id")];
            String job = rs.getString("job");

            staff.getInternalRolesMap()
                .computeIfAbsent(role, k -> new HashSet<>())
                .add(job);
        }, staff.getId());
    }

    private static void saveRoles(StaffMember staff) {
        if (staff.getId() == 0) return;

        PersistenceManager.executeUpdate("DELETE FROM StaffMemberRoles WHERE staff_member_id = ?", staff.getId());

        for (Map.Entry<Role, Set<String>> entry : staff.getInternalRolesMap().entrySet()) {
            int roleId = entry.getKey().getRoleId();
            Set<String> jobs = entry.getValue();

            if (jobs.isEmpty()) {
                PersistenceManager.executeUpdate("INSERT INTO StaffMemberRoles (staff_member_id, role_id) VALUES (?, ?)", staff.getId(), roleId);
            } else {
                for (String job : jobs) {
                    PersistenceManager.executeUpdate("INSERT INTO StaffMemberRoles (staff_member_id, role_id, job) VALUES (?, ?, ?)",
                        staff.getId(), roleId, job);
                }
            }
        }
    }
}
