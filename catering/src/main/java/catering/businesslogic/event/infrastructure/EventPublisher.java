package catering.businesslogic.event.infrastructure;

import catering.businesslogic.event.domain.Event;
import catering.businesslogic.event.domain.Service;
import catering.businesslogic.menu.Menu;

import java.util.ArrayList;

public class EventPublisher {

    private static final ArrayList<EventSubscriber> receivers = new ArrayList<>();

    public static void addEventReceiver(EventSubscriber receiver) {
        if (receiver != null && !receivers.contains(receiver)) {
            receivers.add(receiver);
        }
    }

    public static void removeEventReceiver(EventSubscriber receiver) {
        receivers.remove(receiver);
    }


    // EVENT RELATED

    public static void notifyEventCreated(Event event) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateEventCreated(event);
        }
    }

    public static void notifyEventModified(Event event) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateEventModified(event);
        }
    }

    public static void notifyEventDeleted(Event event) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateEventDeleted(event);
        }
    }


    // SERVICE RELATED

    public static void notifyServiceCreated(Event event, Service service) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateServiceCreated(event, service);
        }
    }

    public static void notifyServiceModified(Service service) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateServiceModified(service);
        }
    }

    public static void notifyServiceDeleted(Service service) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateServiceDeleted(service);
        }
    }


    // MENU RELATED

    public static void notifyMenuAssigned(Service service, Menu menu) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateMenuAssigned(service, menu);
        }
    }

    public static void notifyMenuRemoved(Service service) {
        for (EventSubscriber receiver : receivers) {
            receiver.updateMenuRemoved(service);
        }
    }
}
