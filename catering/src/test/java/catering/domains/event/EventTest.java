package catering.domains.event;

import catering.app.CatERing;
import catering.domains.event.domain.Event;
import catering.domains.event.domain.EventManager;
import catering.domains.event.domain.Service;
import catering.domains.event.infrastructure.EventDAO;
import catering.domains.event.infrastructure.ServiceDAO;
import catering.domains.menu.Menu;
import catering.domains.staffmember.domain.StaffMember;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.exceptions.UseCaseLogicException;
import catering.persistence.PersistenceManager;
import catering.utils.DateUtils;
import catering.utils.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    private static final Logger LOGGER = LogManager.getLogger(EventTest.class);

    private static CatERing app;
    private static StaffMember organizer;
    private EventManager eventManager;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
        organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        assertNotNull(organizer, "Organizer user should be loaded for tests");

        LOGGER.info("Starting test: EventManagerIntegrationTest");
    }

    @BeforeEach
    void setup() {
        // Re-initialize database for each test to ensure clean state
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance(); // Re-get instance after db reset
        eventManager = app.getEventManager(); // Get the EventManager instance from CatERing
        eventManager.setSelectedEvent(null); // Ensure no event is pre-selected
        eventManager.setCurrentService(null); // Ensure no service is pre-selected
    }

    @Test
    void createEvent() {
        try {
            String eventName = "New Test Event";
            Date startDate = DateUtils.safeValueOf("2025-07-10");
            Date endDate = DateUtils.safeValueOf("2025-07-11");

            Event newEvent = eventManager.createEvent(eventName, startDate, endDate, organizer);

            assertNotNull(newEvent, "Event should be created successfully");
            assertEquals(eventName, newEvent.getName());
            assertEquals(startDate, newEvent.getDateStart());
            assertEquals(endDate, newEvent.getDateEnd());
            assertEquals(organizer.getId(), newEvent.getChef().getId());

            // Verify persistence
            Event loadedEvent = EventDAO.loadById(newEvent.getId());
            assertNotNull(loadedEvent, "Created event should be persisted in the database");
            assertEquals(newEvent.getName(), loadedEvent.getName());

            assertEquals(newEvent, eventManager.getSelectedEvent(), "Newly created event should be selected");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void selectEvent() {
        // Create an event first to select it
        Event eventToSelect = eventManager.createEvent("Event to Select", DateUtils.safeValueOf("2025-08-01"), DateUtils.safeValueOf("2025-08-02"), organizer);
        assertNotNull(eventToSelect, "Setup event should be created");

        eventManager.selectEvent(eventToSelect);
        assertEquals(eventToSelect, eventManager.getSelectedEvent(), "Selected event should match the one passed");
        assertNull(eventManager.getCurrentService(), "Current service should be nullified after selecting a new event");
    }

    @Test
    void createService() throws UseCaseLogicException {
        Event parentEvent = eventManager.createEvent("Parent Event", DateUtils.safeValueOf("2025-09-01"), DateUtils.safeValueOf("2025-09-02"), organizer);
        assertNotNull(parentEvent, "Parent event for service creation should be created");
        eventManager.selectEvent(parentEvent);

        String serviceName = "Dinner Service";
        Date serviceDate = DateUtils.safeValueOf("2025-09-01");
        Time timeStart = Time.valueOf("19:00:00");
        Time timeEnd = Time.valueOf("22:00:00");
        String location = "Grand Ballroom";

        Service newService = eventManager.createService(serviceName, serviceDate, timeStart, timeEnd, location);

        assertNotNull(newService, "Service should be created successfully");
        assertEquals(serviceName, newService.getName());
        assertEquals(serviceDate, newService.getDate());
        assertEquals(location, newService.getLocation());
        assertEquals(parentEvent.getId(), newService.getEventId());

        Service loadedService = ServiceDAO.loadById(newService.getId());
        assertNotNull(loadedService, "Created service should be persisted in the database");
        assertEquals(newService.getName(), loadedService.getName());

        assertEquals(newService, eventManager.getCurrentService(), "Newly created service should be current service");
        assertTrue(parentEvent.getServices().contains(newService), "Parent event should contain the new service");
    }

    @Test
    void createServiceWhenNoEventSelected() {
        eventManager.setSelectedEvent(null); // Ensure no event is selected

        assertThrows(UseCaseLogicException.class, () ->
            eventManager.createService(
                "Service Name",
                DateUtils.safeValueOf("2025-07-15"),
                Time.valueOf("10:00:00"),
                Time.valueOf("11:00:00"),
                "Location"),
            "Should throw UseCaseLogicException when no event is selected"
        );
    }

    @Test
    void modifyEvent() {
        // Create an event to modify
        Event existingEvent = eventManager.createEvent("Existing Event", DateUtils.safeValueOf("2025-10-01"), DateUtils.safeValueOf("2025-10-02"), organizer);
        assertNotNull(existingEvent, "Existing event for modification should be created");

        String newName = "Updated Event Name";
        Date newDateStart = DateUtils.safeValueOf("2025-10-05");

        eventManager.modifyEvent(existingEvent.getId(), newName, newDateStart);

        Event modifiedEvent = EventDAO.loadById(existingEvent.getId());
        assertNotNull(modifiedEvent, "Modified event should still exist");
        assertEquals(newName, modifiedEvent.getName(), "Event name should be updated");
        assertEquals(newDateStart, modifiedEvent.getDateStart(), "Event start date should be updated");
    }

    @Test
    void modifyService() throws UseCaseLogicException {
        // Create and select event and service
        Event parentEvent = eventManager.createEvent("Modify Parent Event", DateUtils.safeValueOf("2025-11-01"), DateUtils.safeValueOf("2025-11-02"), organizer);
        eventManager.selectEvent(parentEvent);
        Service existingService = eventManager.createService("Existing Service", DateUtils.safeValueOf("2025-11-01"), Time.valueOf("10:00:00"), Time.valueOf("11:00:00"), "Old Location");
        assertNotNull(existingService, "Existing service for modification should be created");

        String newName = "Updated Service Name";
        Date newDate = DateUtils.safeValueOf("2025-11-03");
        String newLocation = "New Location Hall";

        Service modifiedService = eventManager.modifyService(existingService.getId(), newName, newDate, newLocation, 0);

        assertNotNull(modifiedService, "Modified service should not be null");
        assertEquals(newName, modifiedService.getName(), "Service name should be updated");
        assertEquals(newDate, modifiedService.getDate(), "Service date should be updated");
        assertEquals(newLocation, modifiedService.getLocation(), "Service location should be updated");

        Service loadedService = ServiceDAO.loadById(existingService.getId());
        assertEquals(newName, loadedService.getName()); // Verify persistence
    }

    @Test
    void assignMenuWhenNoServiceSelected() {
        // Create and select event, but no service
        Event parentEvent = eventManager.createEvent("No Service Event", DateUtils.safeValueOf("2026-01-01"), DateUtils.safeValueOf("2026-01-02"), organizer);
        eventManager.selectEvent(parentEvent);
        eventManager.setCurrentService(null); // Explicitly ensure no service is selected

        Menu dummyMenu = new Menu(organizer,"Dummy Menu"); // Just a dummy, won't be persisted

        assertThrows(UseCaseLogicException.class, () ->
            eventManager.assignMenu(dummyMenu), "Should throw UseCaseLogicException when no service is selected"
        );
    }

    @Test
    void deleteService() throws UseCaseLogicException {
        // Create and select event and service
        Event parentEvent = eventManager.createEvent("Delete Parent Event", DateUtils.safeValueOf("2026-02-01"), DateUtils.safeValueOf("2026-02-02"), organizer);
        eventManager.selectEvent(parentEvent);
        Service serviceToDelete = eventManager.createService("Service to Delete", DateUtils.safeValueOf("2026-02-01"), Time.valueOf("10:00:00"), Time.valueOf("11:00:00"), "Location");
        assertNotNull(serviceToDelete, "Service to delete should be created");

        boolean result = eventManager.deleteService(serviceToDelete.getId());

        assertTrue(result, "Service deletion should be successful");
        assertNull(ServiceDAO.loadById(serviceToDelete.getId()), "Service should be deleted from database");
        assertFalse(parentEvent.getServices().contains(serviceToDelete), "Parent event should no longer contain the deleted service");
    }

    @Test
    void deleteEvent() {
        // Create an event to delete
        Event eventToDelete = eventManager.createEvent("Event to Delete", DateUtils.safeValueOf("2026-03-01"), DateUtils.safeValueOf("2026-03-02"), organizer);
        assertNotNull(eventToDelete, "Event to delete should be created");

        boolean result = eventManager.deleteEvent(eventToDelete.getId());

        assertTrue(result, "Event deletion should be successful");
        assertNull(EventDAO.loadById(eventToDelete.getId()), "Event should be deleted from database");
        assertNull(eventManager.getSelectedEvent(), "Selected event should be null after deletion");
        assertNull(eventManager.getCurrentService(), "Current service should be null after event deletion");
    }
}
