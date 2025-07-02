package catering.businesslogic.kitchen.infrastructure;

import catering.businesslogic.kitchen.domain.Assignment;
import catering.businesslogic.shift.ShiftDAO;
import catering.businesslogic.staffmember.StaffMemberDAO;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Assignment
 */
public class AssignmentDAO {

    public static List<Assignment> loadAllBySummarySheetId(int summarySheetID) {
        String query = "SELECT * FROM Assignment WHERE sumsheet_id = ?";
        List<Assignment> assignments = new ArrayList<>();
        List<Integer> shiftIds = new ArrayList<>();
        List<Integer> taskIds = new ArrayList<>();
        List<Integer> cookIds = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            Assignment a = new Assignment();
            a.setId(rs.getInt("id"));

            shiftIds.add(rs.getInt("shift_id"));
            taskIds.add(rs.getInt("task_id"));
            cookIds.add(rs.getInt("cook_id"));

            assignments.add(a);
        }, summarySheetID);

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            a.setShift(ShiftDAO.loadById(shiftIds.get(i)));
            a.setTask(KitchenTaskDAO.loadById(taskIds.get(i)));
            int cookId = cookIds.get(i);
            a.setCook(cookId > 0 ? StaffMemberDAO.loadById(cookId) : null);
        }

        return assignments;
    }

    public static void save(int summarySheetID, Assignment assignment) {
        String query = "INSERT INTO Assignment (sumsheet_id, shift_id, task_id, cook_id) VALUES (?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query,
            summarySheetID,
            assignment.getShift().getId(),
            assignment.getTask().getId(),
            assignment.getCook() != null ? assignment.getCook().getId() : 0);
        assignment.setId(PersistenceManager.getLastId());
    }

    public static void saveAllNewAssignments(int summarySheetID, List<Assignment> assignments) {
        String query = "INSERT INTO Assignment (sumsheet_id, shift_id, task_id, cook_id) VALUES (?, ?, ?, ?)";
        PersistenceManager.executeBatchUpdate(query, assignments.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                Assignment a = assignments.get(i);
                ps.setInt(1, summarySheetID);
                ps.setInt(2, a.getShift().getId());
                ps.setInt(3, a.getTask().getId());
                ps.setInt(4, a.getCook() != null ? a.getCook().getId() : 0);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int i) throws SQLException {
                assignments.get(i).setId(rs.getInt(1));
            }
        });
    }

    public static void update(Assignment assignment) {
        String query = "UPDATE Assignment SET shift_id = ?, cook_id = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query,
            assignment.getShift().getId(),
            assignment.getCook() != null ? assignment.getCook().getId() : 0,
            assignment.getId());
    }

    public static void delete(Assignment assignment) {
        String query = "DELETE FROM Assignment WHERE id = ?";
        PersistenceManager.executeUpdate(query, assignment.getId());
    }
}
