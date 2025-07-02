package catering.domains.kitchen.infrastructure;

import catering.domains.event.infrastructure.ServiceDAO;
import catering.domains.kitchen.domain.SummarySheet;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for loading and saving SummarySheet entities.
 */
public class SummarySheetDAO {

    public static void save(SummarySheet sheet) {
        String insert = "INSERT INTO SummarySheets (service_id, owner_id) VALUES (?, ?)";

        int[] result = PersistenceManager.executeBatchUpdate(insert, 1, new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, sheet.getService().getId());
                ps.setInt(2, sheet.getOwner().getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                if (count == 0) {
                    sheet.setId(rs.getInt(1));
                }
            }
        });

        if (result[0] > 0) {
            if (!sheet.getTasks().isEmpty()) {
                KitchenTaskDAO.saveAllNewTasks(sheet.getId(), sheet.getTasks());
            }
            if (!sheet.getAssignments().isEmpty()) {
                AssignmentDAO.saveAllNewAssignments(sheet.getId(), sheet.getAssignments());
            }
        }
    }

    public static List<SummarySheet> loadAll() {
        return loadSummarySheets("SELECT * FROM SummarySheets");
    }

    public static SummarySheet loadById(int id) {
        List<SummarySheet> sheets = loadSummarySheets("SELECT * FROM SummarySheets WHERE id = ?", id);
        return sheets.isEmpty() ? null : sheets.get(0);
    }

    public static List<SummarySheet> loadByServiceId(int serviceId) {
        return loadSummarySheets("SELECT * FROM SummarySheets WHERE service_id = ?", serviceId);
    }

    public static void updateTaskPositions(SummarySheet sheet) {
        String query = "UPDATE Tasks SET position = ? WHERE id = ?";

        PersistenceManager.executeBatchUpdate(query, sheet.getTasks().size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, i);
                ps.setInt(2, sheet.getTasks().get(i).getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) {
                // No ID generation
            }
        });
    }

    // Helper method shared across load methods
    private static List<SummarySheet> loadSummarySheets(String query, Object... params) {
        List<SummarySheet> sheets = new ArrayList<>();
        List<Integer> serviceIds = new ArrayList<>();
        List<Integer> ownerIds = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            SummarySheet s = new SummarySheet();
            s.setId(rs.getInt("id"));
            sheets.add(s);
            serviceIds.add(rs.getInt("service_id"));
            ownerIds.add(rs.getInt("owner_id"));
        }, params);

        for (int i = 0; i < sheets.size(); i++) {
            SummarySheet sheet = sheets.get(i);
            sheet.setService(ServiceDAO.loadById(serviceIds.get(i)));
            sheet.setOwner(StaffMemberDAO.loadById(ownerIds.get(i)));
            sheet.setTasks(KitchenTaskDAO.loadAllBySummarySheetId(sheet.getId()));
            sheet.setAssignments(AssignmentDAO.loadAllBySummarySheetId(sheet.getId()));
        }

        return sheets;
    }
}
