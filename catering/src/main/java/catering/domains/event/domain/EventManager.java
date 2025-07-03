package catering.domains.event.domain;

import catering.domains.event.infrastructure.EventDAO;
import catering.domains.event.infrastructure.EventPublisher;
import catering.domains.menu.infrastructure.MenuDAO;
import catering.exceptions.UseCaseLogicException;
import catering.domains.event.infrastructure.ServiceDAO;
import catering.domains.menu.domain.Menu;
import catering.domains.staffmember.domain.StaffMember;
import catering.utils.LogManager;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Data;

@Data
public class EventManager {

    private final Logger LOGGER = LogManager.getLogger(EventManager.class);

    private Event selectedEvent;
    private Service currentService;


    // EVENTS
    public void selectEvent(Event event) {
        LOGGER.info("Selecting event '" + event.getName() + "' (ID: " + event.getId() + ")");
        this.selectedEvent = event;
        this.currentService = null;
    }

    public Event createEvent(String name, Date dateStart, Date dateEnd, StaffMember chef) {
        try {
            Event event = new Event(name, dateStart, dateEnd, chef);
            EventDAO.save(event);

            this.selectedEvent = event;
            this.currentService = null;

            EventPublisher.notifyEventCreated(event);

            return event;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create event '" + name + "'", e);
            return null;
        }
    }

    public boolean deleteEvent(int eventId) {
        Event event = EventDAO.loadById(eventId);
        if (event == null) return false;

        boolean success = EventDAO.delete(event);
        if (success) {
            if (selectedEvent != null && selectedEvent.getId() == eventId) {
                selectedEvent = null;
                currentService = null;
            }

            EventPublisher.notifyEventDeleted(event);
        }

        return success;
    }

    public void modifyEvent(int eventId, String name, Date dateStart) {
        Event event = EventDAO.loadById(eventId);
        if (event != null) {
            event.setName(name);
            event.setDateStart(dateStart);
            EventDAO.update(event);

            EventPublisher.notifyEventModified(event);

            if (selectedEvent != null && selectedEvent.getId() == eventId) {
                this.selectedEvent = event;
            }
        }
    }

    public ArrayList<Event> getAllEvents() {
        return new ArrayList<>(EventDAO.loadAll());
    }


    // SERVICE

    public Service createService(String name, Date date, Time timeStart, Time timeEnd, String location)
        throws UseCaseLogicException {
        if (selectedEvent == null) {
            throw new UseCaseLogicException("Cannot create service: no event selected");
        }

        try {
            Service service = new Service(name, date, timeStart, timeEnd, location, selectedEvent.getId());
            ServiceDAO.save(service);

            selectedEvent.addService(service);
            this.currentService = service;

            EventPublisher.notifyServiceCreated(selectedEvent, service);

            return service;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create service '" + name + "'", e);
            return null;
        }
    }

    public Service modifyService(int serviceId, String name, Date date, String location, int menuId) {
        Service service = ServiceDAO.loadById(serviceId);
        if (service != null) {
            service.setName(name);
            service.setDate(date);
            service.setLocation(location);

            if (menuId > 0 && (service.getMenuId() == 0 || service.getMenuId() != menuId)) {
                try {
                    Menu menu = MenuDAO.load(menuId);
                    service.setMenu(menu);
                } catch (Exception e) {
                    LOGGER.warning("Error loading menu ID " + menuId + ": " + e.getMessage());
                }
            }

            ServiceDAO.update(service);

            if (currentService != null && currentService.getId() == serviceId) {
                currentService = service;
            }

            EventPublisher.notifyServiceModified(service);
        }

        return service;
    }

    public boolean deleteService(int serviceId) {
        if (selectedEvent == null) return false;

        Service service = ServiceDAO.loadById(serviceId);
        if (service == null) return false;

        boolean success = ServiceDAO.delete(serviceId);
        if (success) {
            selectedEvent.removeService(service);
            if (currentService != null && currentService.getId() == serviceId) {
                currentService = null;
            }

            EventPublisher.notifyServiceDeleted(service);
        }

        return success;
    }

    public void setSelectedServiceIndex(int serviceId) {
        if (selectedEvent != null && selectedEvent.getServices() != null) {
            for (Service service : selectedEvent.getServices()) {
                if (service.getId() == serviceId) {
                    currentService = service;
                    return;
                }
            }
        }
    }


    // MENU

    public void assignMenu(Menu menu) throws UseCaseLogicException {
        if (selectedEvent == null) {
            throw new UseCaseLogicException("No event selected");
        }
        if (currentService == null) {
            throw new UseCaseLogicException("No service selected");
        }

        currentService.assignMenuToService(menu);
        ServiceDAO.update(currentService);

        EventPublisher.notifyMenuAssigned(currentService, menu);
    }

    public boolean removeMenu() {
        if (currentService == null) return false;

        currentService.removeMenu();
        ServiceDAO.update(currentService);

        EventPublisher.notifyMenuRemoved(currentService);
        return true;
    }
}
