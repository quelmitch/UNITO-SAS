package catering.domains.menu.infrastructure;

import catering.domains.menu.domain.Section;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {

    public static void create(int menuId, Section section, int posInMenu) {
        String query = "INSERT INTO MenuSections (menu_id, name, position) VALUES (?, ?, ?)";
        PersistenceManager.executeUpdate(query, menuId, section.getName(), posInMenu);
        section.setId(PersistenceManager.getLastId());

        if (!section.getMenuItems().isEmpty()) {
            MenuItemDAO.create(menuId, section.getId(), section.getMenuItems());
        }
    }

    public static void create(int menuId, List<Section> sections) {
        String query = "INSERT INTO MenuSections (menu_id, name, position) VALUES (?, ?, ?)";
        PersistenceManager.executeBatchUpdate(query, sections.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, menuId);
                ps.setString(2, sections.get(i).getName());
                ps.setInt(3, i);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                if (rs.next()) {
                    sections.get(count).setId(rs.getInt(1));
                }
            }
        });

        for (Section s : sections) {
            if (!s.getMenuItems().isEmpty()) {
                MenuItemDAO.create(menuId, s.getId(), s.getMenuItems());
            }
        }
    }

    public static ArrayList<Section> loadSections(int menuId) {
        String query = "SELECT * FROM MenuSections WHERE menu_id = ? ORDER BY position";
        ArrayList<Section> result = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            Section s = new Section(rs.getString("name"));
            s.setId(rs.getInt("id"));
            result.add(s);
        }, menuId);

        for (Section s : result) {
            s.setMenuItems(MenuItemDAO.loadItems(menuId, s.getId()));
        }

        return result;
    }

    public static void delete(int menuId, Section s) {
        PersistenceManager.executeUpdate("DELETE FROM MenuItems WHERE section_id = ? AND menu_id = ?", s.getId(), menuId);
        PersistenceManager.executeUpdate("DELETE FROM MenuSections WHERE id = ?", s.getId());
    }

    public static void saveName(Section s) {
        String query = "UPDATE MenuSections SET name = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, s.getName(), s.getId());
    }

    public static void saveItemOrder(Section s) {
        String query = "UPDATE MenuItems SET position = ? WHERE id = ?";
        PersistenceManager.executeBatchUpdate(query, s.getMenuItems().size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, i);
                ps.setInt(2, s.getMenuItems().get(i).getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) {
                // no-op
            }
        });
    }
}
