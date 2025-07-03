package catering.domains.menu.infrastructure;

import catering.domains.menu.domain.MenuItem;
import catering.domains.menu.domain.Section;
import catering.domains.menu.domain.Menu;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

public class MenuDAO {

    public static void create(Menu m) {
        String query = "INSERT INTO Menus (title, owner_id, published) VALUES (?, ?, ?);";

        int[] result = PersistenceManager.executeBatchUpdate(query, 1, new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setString(1, m.getTitle());
                ps.setInt(2, m.getOwner().getId());
                ps.setBoolean(3, m.isPublished());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                if (rs.next()) {
                    m.setId(rs.getInt(1));
                }
            }
        });

        if (result[0] > 0) {
            saveFeaturesToDB(m);
            if (!m.getSections().isEmpty()) {
                SectionDAO.create(m.getId(), m.getSections());
            }
            if (!m.getFreeItems().isEmpty()) {
                MenuItemDAO.create(m.getId(), 0, m.getFreeItems());
            }
        }
    }

    public static Menu load(Integer id) {
        String query = "SELECT * FROM Menus WHERE id = ?";
        Menu m = new Menu();

        PersistenceManager.executeQuery(query, rs -> {
            m.setId(id);
            m.setTitle(rs.getString("title"));
            m.setPublished(rs.getBoolean("published"));
            m.setOwner(StaffMemberDAO.loadById(rs.getInt("owner_id")));

            m.setSections(SectionDAO.loadSections(id));
            m.setFreeItems(MenuItemDAO.loadItems(id, 0));
            loadFeaturesFromDB(m);
            checkIfMenuIsInUse(m);
        }, id);

        return m;
    }

    public static void delete(Menu m) {
        PersistenceManager.executeUpdate("DELETE FROM MenuItems WHERE menu_id = ?", m.getId());
        PersistenceManager.executeUpdate("DELETE FROM MenuSections WHERE menu_id = ?", m.getId());
        PersistenceManager.executeUpdate("DELETE FROM MenuFeatures WHERE menu_id = ?", m.getId());
        PersistenceManager.executeUpdate("DELETE FROM Menus WHERE id = ?", m.getId());
    }

    public static void saveTitle(Menu m) {
        PersistenceManager.executeUpdate(
            "UPDATE Menus SET title = ? WHERE id = ?",
            m.getTitle(), m.getId()
        );
    }

    public static void savePublished(Menu m) {
        PersistenceManager.executeUpdate(
            "UPDATE Menus SET published = ? WHERE id = ?",
            m.isPublished(), m.getId()
        );
    }

    public static void saveFeatures(Menu m) {
        PersistenceManager.executeUpdate(
            "DELETE FROM MenuFeatures WHERE menu_id = ?", m.getId()
        );
        saveFeaturesToDB(m);
    }

    public static void saveSectionOrder(Menu m) {
        String query = "UPDATE MenuSections SET position = ? WHERE id = ?";
        PersistenceManager.executeBatchUpdate(query, m.getSections().size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, i);
                ps.setInt(2, m.getSections().get(i).getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) {
                // No-op
            }
        });
    }

    public static void saveFreeItemOrder(Menu m) {
        String query = "UPDATE MenuItems SET position = ? WHERE id = ?";
        PersistenceManager.executeBatchUpdate(query, m.getFreeItems().size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, i);
                ps.setInt(2, m.getFreeItems().get(i).getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) {
                // No-op
            }
        });
    }


    // HELPERS

    private static void saveFeaturesToDB(Menu m) {
        String query = "INSERT INTO MenuFeatures (menu_id, name, value) VALUES (?, ?, ?)";
        Map<Menu.Feature, Boolean> featureMap = m.getFeatures();

        PersistenceManager.executeBatchUpdate(query, featureMap.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                Menu.Feature feature = Menu.Feature.values()[i];
                ps.setInt(1, m.getId());
                ps.setString(2, feature.name());
                ps.setBoolean(3, featureMap.getOrDefault(feature, false));
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) {
                // No-op
            }
        });
    }

    private static void loadFeaturesFromDB(Menu m) {
        String query = "SELECT name, value FROM MenuFeatures WHERE menu_id = ?";
        EnumMap<Menu.Feature, Boolean> featureMap = new EnumMap<>(Menu.Feature.class);

        PersistenceManager.executeQuery(query, rs -> {
            String name = rs.getString("name");
            boolean value = rs.getBoolean("value");
            try {
                Menu.Feature feature = Menu.Feature.valueOf(name);
                featureMap.put(feature, value);
            } catch (IllegalArgumentException e) {
                // Unknown feature name in DB, ignore
            }
        }, m.getId());

        m.setFeatures(featureMap);
    }

    private static void checkIfMenuIsInUse(Menu m) {
        String query = "SELECT 1 FROM Services WHERE approved_menu_id = ?";
        PersistenceManager.executeQuery(query, rs -> m.setInUse(true), m.getId());
    }
}
