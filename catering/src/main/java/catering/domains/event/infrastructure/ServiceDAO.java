package catering.domains.event.infrastructure;

import catering.domains.event.domain.Service;
import catering.domains.menu.domain.Menu;
import catering.domains.menu.infrastructure.MenuDAO;
import catering.persistence.PersistenceManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ServiceDAO {

    private final static Logger LOGGER = catering.utils.LogManager.getLogger(ServiceDAO.class);

    public static void save(Service service) {
        String query = "INSERT INTO Services (event_id, name, service_date, time_start, time_end, location) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        Long dateTimestamp = (service.getDate() != null) ? service.getDate().getTime() : null;

        PersistenceManager.executeUpdate(query,
            service.getEventId(),
            service.getName(),
            dateTimestamp,
            service.getTimeStart(),
            service.getTimeEnd(),
            service.getLocation());

        service.setId(PersistenceManager.getLastId());
    }

    public static void update(Service service) {
        String query = "UPDATE Services SET name = ?, service_date = ?, time_start = ?, time_end = ?, location = ? " +
            "WHERE id = ?";

        Long dateTimestamp = (service.getDate() != null) ? service.getDate().getTime() : null;

        PersistenceManager.executeUpdate(query,
            service.getName(),
            dateTimestamp,
            service.getTimeStart(),
            service.getTimeEnd(),
            service.getLocation(),
            service.getId());
    }

    public static boolean delete(int serviceId) {
        String query = "DELETE FROM Services WHERE id = ?";
        return PersistenceManager.executeUpdate(query, serviceId) > 0;
    }

    public static Service loadById(int id) {
        String query = "SELECT * FROM Services WHERE id = ?";
        return loadServiceByQuery(query, id);
    }

    public static Service loadByName(String name) {
        String query = "SELECT * FROM Services WHERE name = ?";
        return loadServiceByQuery(query, name);
    }

    public static ArrayList<Service> loadAllForEvent(int eventId) {
        ArrayList<Service> services = new ArrayList<>();
        String query = "SELECT * FROM Services WHERE event_id = ? ORDER BY service_date, time_start";

        PersistenceManager.executeQuery(query, rs -> {
            Service s = mapResultSetToService(rs);
            services.add(s);
        }, eventId);

        return services;
    }

    public static void assignMenu(Service service, Menu menu) {
        String query = "UPDATE Services SET approved_menu_id = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, menu.getId(), service.getId());
    }

    public static void removeMenu(Service service) {
        String query = "UPDATE Services SET approved_menu_id = 0 WHERE id = ?";
        PersistenceManager.executeUpdate(query, service.getId());
    }

    public static void approveMenu(Service service) {
        if (service.getMenu() != null) {
            assignMenu(service, service.getMenu());
        }
    }


    // HELPERS
    private static Service loadServiceByQuery(String query, Object param) {
        final Service[] holder = new Service[1];

        PersistenceManager.executeQuery(query, rs ->
            holder[0] = mapResultSetToService(rs), param
        );

        return holder[0];
    }

    private static Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));

        try {
            String dateStr = rs.getString("service_date");
            String startTimeStr = rs.getString("time_start");
            String endTimeStr = rs.getString("time_end");

            if (dateStr != null && !dateStr.isEmpty()) s.setDate(Date.valueOf(dateStr));
            if (startTimeStr != null && !startTimeStr.isEmpty()) s.setTimeStart(Time.valueOf(startTimeStr));
            if (endTimeStr != null && !endTimeStr.isEmpty()) s.setTimeEnd(Time.valueOf(endTimeStr));

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Failed to parse date/time for service ID " + s.getId());
        }

        s.setLocation(rs.getString("location"));
        s.setEventId(rs.getInt("event_id"));

        int menuId = rs.getInt("approved_menu_id");
        if (menuId > 0) {
            try {
                s.setMenu(MenuDAO.load(menuId));
            } catch (Exception e) {
                LOGGER.warning("Could not load menu ID " + menuId + " for service ID " + s.getId());
            }
        }

        return s;
    }
}
