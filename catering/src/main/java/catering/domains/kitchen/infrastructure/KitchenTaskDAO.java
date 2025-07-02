package catering.domains.kitchen.infrastructure;

import catering.domains.kitchen.domain.KitchenTask;
import catering.domains.recipe.infrastructure.PreparationDAO;
import catering.domains.recipe.infrastructure.RecipeDAO;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for KitchenTask persistence
 */
public class KitchenTaskDAO {

    public static void saveNewTask(int summarySheetId, KitchenTask task, int position) {
        String query = "INSERT INTO Tasks (sumsheet_id, kitchenproc_id, description, type, position, ready, quantity, portions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PersistenceManager.executeUpdate(query,
            summarySheetId,
            task.getKitchenProcess().getId(),
            task.getDescription(),
            task.getKitchenProcess().isRecipe(),
            position,
            task.isReady(),
            task.getQuantity(),
            task.getPortions());

        task.setId(PersistenceManager.getLastId());
    }

    public static void saveAllNewTasks(int summarySheetId, List<KitchenTask> tasks) {
        String query = "INSERT INTO Tasks (sumsheet_id, kitchenproc_id, description, type, position, ready, quantity, portions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PersistenceManager.executeBatchUpdate(query, tasks.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                KitchenTask t = tasks.get(i);
                ps.setInt(1, summarySheetId);
                ps.setInt(2, t.getKitchenProcess().getId());
                ps.setString(3, t.getDescription());
                ps.setBoolean(4, t.getKitchenProcess().isRecipe());
                ps.setInt(5, i); // position
                ps.setBoolean(6, t.isReady());
                ps.setInt(7, t.getQuantity());
                ps.setInt(8, t.getPortions());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int i) throws SQLException {
                tasks.get(i).setId(rs.getInt(1));
            }
        });
    }

    public static List<KitchenTask> loadAllBySummarySheetId(int summarySheetId) {
        String query = "SELECT * FROM Tasks WHERE sumsheet_id = ? ORDER BY position";
        List<KitchenTask> tasks = new ArrayList<>();
        List<Integer> processIds = new ArrayList<>();
        List<Boolean> types = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            KitchenTask task = new KitchenTask();
            task.setId(rs.getInt("id"));
            task.setDescription(rs.getString("description"));
            task.setPortions(rs.getInt("portions"));
            task.setQuantity(rs.getInt("quantity"));
            task.setReady(rs.getBoolean("ready"));
            boolean type = rs.getBoolean("type");
            task.setType(type);

            processIds.add(rs.getInt("kitchenproc_id"));
            types.add(type);
            tasks.add(task);
        }, summarySheetId);

        for (int i = 0; i < tasks.size(); i++) {
            int procId = processIds.get(i);
            boolean isRecipe = types.get(i);
            tasks.get(i).setKitchenProcess(isRecipe
                ? RecipeDAO.loadRecipeById(procId)
                : PreparationDAO.loadPreparationById(procId));
        }

        return tasks;
    }

    public static KitchenTask loadById(int id) {
        String query = "SELECT * FROM Tasks WHERE id = ?";
        final KitchenTask[] taskHolder = new KitchenTask[1];
        final int[] procId = new int[1];
        final boolean[] typeHolder = new boolean[1];

        PersistenceManager.executeQuery(query, rs -> {
            KitchenTask task = new KitchenTask();
            task.setId(rs.getInt("id"));
            task.setDescription(rs.getString("description"));
            task.setQuantity(rs.getInt("quantity"));
            task.setPortions(rs.getInt("portions"));
            task.setReady(rs.getBoolean("ready"));
            boolean type = rs.getBoolean("type");

            task.setType(type);
            procId[0] = rs.getInt("kitchenproc_id");
            typeHolder[0] = type;
            taskHolder[0] = task;
        }, id);

        if (taskHolder[0] == null) return null;

        KitchenTask task = taskHolder[0];
        task.setKitchenProcess(typeHolder[0]
            ? RecipeDAO.loadRecipeById(procId[0])
            : PreparationDAO.loadPreparationById(procId[0]));

        return task;
    }

    public static void update(KitchenTask task) {
        String query = "UPDATE Tasks SET description = ?, quantity = ?, portions = ?, ready = ? WHERE id = ?";

        PersistenceManager.executeUpdate(query,
            task.getDescription(),
            task.getQuantity(),
            task.getPortions(),
            task.isReady(),
            task.getId());
    }
}
