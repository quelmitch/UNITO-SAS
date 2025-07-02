package catering.businesslogic.event.infrastructure;

import catering.businesslogic.event.domain.Event;
import catering.businesslogic.event.domain.Service;
import catering.businesslogic.staffmember.StaffMemberDAO;
import catering.persistence.PersistenceManager;
import catering.util.DateUtils;
import catering.util.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EventDAO {
    private final static Logger LOGGER = LogManager.getLogger(EventDAO.class);

    public static void save(Event event) {
        String query = "INSERT INTO Events (name, date_start, date_end, chef_id) VALUES (?, ?, ?, ?)";

        Long startTimestamp = event.getDateStart() != null ? event.getDateStart().getTime() : null;
        Long endTimestamp = event.getDateEnd() != null ? event.getDateEnd().getTime() : null;

        PersistenceManager.executeUpdate(query,
            event.getName(),
            startTimestamp,
            endTimestamp,
            event.getChefId());

        event.setId(PersistenceManager.getLastId());

        LOGGER.info("Saved event: " + event.getName() + " (ID: " + event.getId() + ")");
    }

    public static void update(Event event) {
        String query = "UPDATE Events SET name = ?, date_start = ?, date_end = ?, chef_id = ? WHERE id = ?";

        Long startTimestamp = event.getDateStart() != null ? event.getDateStart().getTime() : null;
        Long endTimestamp = event.getDateEnd() != null ? event.getDateEnd().getTime() : null;

        PersistenceManager.executeUpdate(query,
            event.getName(),
            startTimestamp,
            endTimestamp,
            event.getChefId(),
            event.getId());

        LOGGER.info("Updated event: " + event.getName() + " (ID: " + event.getId() + ")");
    }

    public static boolean delete(Event event) {
        // Delete all services for the event using ServiceDAO
        for (Service service : event.getServices()) {
            ServiceDAO.delete(service.getId());
        }

        event.getServices().clear();

        // Delete the event
        String query = "DELETE FROM Events WHERE id = ?";
        boolean success = PersistenceManager.executeUpdate(query, event.getId()) > 0;

        if (success) {
            LOGGER.info("Deleted event: " + event.getName() + " (ID: " + event.getId() + ")");
        }

        return success;
    }

    public static List<Event> loadAll() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM Events ORDER BY date_start DESC";

        PersistenceManager.executeQuery(query, rs -> {
            Event e = fromResultSet(rs);
            events.add(e);
        });

        // Load services for each event using ServiceDAO
        for (Event e : events) {
            try {
                e.setServices(ServiceDAO.loadAllForEvent(e.getId()));
            } catch (Exception ex) {
                e.setServices(new ArrayList<>());
            }
        }

        return events;
    }

    public static Event loadById(int id) {
        String query = "SELECT * FROM Events WHERE id = ?";
        return loadEventByQuery(query, id);
    }

    public static Event loadByName(String name) {
        String query = "SELECT * FROM Events WHERE name = ?";
        return loadEventByQuery(query, name);
    }


    // HELPERS

    private static Event loadEventByQuery(String query, Object param) {
        final Event[] eventHolder = new Event[1];

        PersistenceManager.executeQuery(query, rs -> eventHolder[0] = fromResultSet(rs), param);

        Event event = eventHolder[0];

        if (event != null) {
            try {
                event.setServices(ServiceDAO.loadAllForEvent(event.getId()));
            } catch (Exception ex) {
                event.setServices(new ArrayList<>());
            }
        }

        return event;
    }

    private static Event fromResultSet(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setDateStart(DateUtils.getDateFromResultSet(rs, "date_start"));
        e.setDateEnd(DateUtils.getDateFromResultSet(rs, "date_end"));

        try {
            e.setChef(StaffMemberDAO.loadById(rs.getInt("chef_id")));
        } catch (Exception ex) {
            e.setChef(null);
        }

        return e;
    }
}
