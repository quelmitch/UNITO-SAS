package catering.domains.menu.infrastructure;

import catering.domains.menu.domain.MenuItem;
import catering.domains.recipe.infrastructure.RecipeDAO;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MenuItemDAO {

    public static void create(int menuId, int sectionId, ArrayList<MenuItem> items) {
        String query = "INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) VALUES (?, ?, ?, ?, ?)";

        PersistenceManager.executeBatchUpdate(query, items.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                MenuItem item = items.get(i);
                ps.setInt(1, menuId);
                ps.setInt(2, sectionId);
                ps.setString(3, item.getDescription());
                ps.setInt(4, item.getRecipe().getId());
                ps.setInt(5, i);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int i) throws SQLException {
                if (rs.next()) {
                    items.get(i).setId(rs.getInt(1));
                }
            }
        });
    }

    public static void create(int menuId, int sectionId, MenuItem item, int position) {
        String query = "INSERT INTO MenuItems (menu_id, section_id, description, recipe_id, position) VALUES (?, ?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query, menuId, sectionId, item.getDescription(), item.getRecipe().getId(), position);
        item.setId(PersistenceManager.getLastId());
    }

    public static ArrayList<MenuItem> loadItems(int menuId, int sectionId) {
        ArrayList<MenuItem> result = new ArrayList<>();
        ArrayList<Integer> recipeIds = new ArrayList<>();

        String query = "SELECT * FROM MenuItems WHERE menu_id = ? AND section_id = ? ORDER BY position";

        PersistenceManager.executeQuery(query, rs -> {
            MenuItem item = new MenuItem();
            item.setId(rs.getInt("id"));
            item.setDescription(rs.getString("description"));
            result.add(item);
            recipeIds.add(rs.getInt("recipe_id"));
        }, menuId, sectionId);

        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRecipe(RecipeDAO.loadRecipeById(recipeIds.get(i)));
        }

        return result;
    }

    public static void updateSection(int sectionId, MenuItem item) {
        String query = "UPDATE MenuItems SET section_id = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, sectionId, item.getId());
    }

    public static void updateDescription(MenuItem item) {
        String query = "UPDATE MenuItems SET description = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, item.getDescription(), item.getId());
    }

    public static void delete(MenuItem item) {
        String query = "DELETE FROM MenuItems WHERE id = ?";
        PersistenceManager.executeUpdate(query, item.getId());
    }
}
