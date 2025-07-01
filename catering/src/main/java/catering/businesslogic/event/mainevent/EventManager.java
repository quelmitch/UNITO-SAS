package catering.businesslogic.event.mainevent;

import catering.exceptions.UseCaseLogicException;
import catering.businesslogic.event.service.Service;
import catering.businesslogic.event.service.ServiceDAO;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.staffmember.StaffMember;
import catering.util.LogManager;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Data;

@Data
public class EventManager {

    private final Logger LOGGER = LogManager.getLogger(EventManager.class);

    private EventEventNotifier eventEventNotifier; // notification manager

    private Event selectedEvent;
    private Service currentService;

    public EventManager() {
        this.eventEventNotifier = new EventEventNotifier();
    }

    public void addEventReceiver(EventEventReceiver receiver) {
        eventEventNotifier.addEventReceiver(receiver);
    }

    public void removeEventReceiver(EventEventReceiver receiver) {
        eventEventNotifier.removeEventReceiver(receiver);
    }

    public ArrayList<Event> getEvents() {
        return new ArrayList<>(EventDAO.loadAll());
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

    public Event createEvent(String name, Date dateStart, Date dateEnd, StaffMember chef) {
        try {
            Event event = new Event(name, dateStart, dateEnd, chef);
            EventDAO.save(event);

            this.selectedEvent = event;
            this.currentService = null;

            eventEventNotifier.notifyEventCreated(event);

            return event;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create event '" + name + "'", e);
            return null;
        }
    }

    public void selectEvent(Event event) {
        LOGGER.info("Selecting event '" + event.getName() + "' (ID: " + event.getId() + ")");
        this.selectedEvent = event;
        this.currentService = null;
    }

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

            eventEventNotifier.notifyServiceCreated(selectedEvent, service);

            return service;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create service '" + name + "'", e);
            return null;
        }
    }

    public void modifyEvent(int eventId, String name, Date dateStart) {
        Event event = EventDAO.loadById(eventId);
        if (event != null) {
            event.setName(name);
            event.setDateStart(dateStart);
            EventDAO.update(event);

            eventEventNotifier.notifyEventModified(event);

            if (selectedEvent != null && selectedEvent.getId() == eventId) {
                this.selectedEvent = event;
            }
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
                    Menu menu = Menu.load(menuId);
                    service.setMenu(menu);
                } catch (Exception e) {
                    LOGGER.warning("Error loading menu ID " + menuId + ": " + e.getMessage());
                }
            }

            ServiceDAO.update(service);

            if (currentService != null && currentService.getId() == serviceId) {
                currentService = service;
            }

            eventEventNotifier.notifyServiceModified(service);
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

            eventEventNotifier.notifyServiceDeleted(service);
        }

        return success;
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

            eventEventNotifier.notifyEventDeleted(event);
        }

        return success;
    }

    public void assignMenu(Menu menu) throws UseCaseLogicException {
        if (selectedEvent == null) {
            throw new UseCaseLogicException("No event selected");
        }
        if (currentService == null) {
            throw new UseCaseLogicException("No service selected");
        }

        currentService.assignMenuToService(menu);
        ServiceDAO.update(currentService);

        eventEventNotifier.notifyMenuAssigned(currentService, menu);
    }

    public boolean removeMenu() {
        if (currentService == null) return false;

        currentService.removeMenu();
        ServiceDAO.update(currentService);

        eventEventNotifier.notifyMenuRemoved(currentService);
        return true;
    }
}
