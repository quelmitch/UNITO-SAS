package catering.domains.event.infrastructure;

import catering.domains.event.domain.Event;
import catering.domains.event.domain.Service;
import catering.domains.menu.Menu;

import java.util.ArrayList;

public class EventPublisher {

    private static final ArrayList<EventSubscriber> subscribers = new ArrayList<>();

    public static void addSubscriber(EventSubscriber subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public static void removeSubscriber(EventSubscriber subscriber) {
        subscribers.remove(subscriber);
    }


    // EVENT RELATED

    public static void notifyEventCreated(Event event) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateEventCreated(event);
        }
    }

    public static void notifyEventModified(Event event) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateEventModified(event);
        }
    }

    public static void notifyEventDeleted(Event event) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateEventDeleted(event);
        }
    }


    // SERVICE RELATED

    public static void notifyServiceCreated(Event event, Service service) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateServiceCreated(event, service);
        }
    }

    public static void notifyServiceModified(Service service) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateServiceModified(service);
        }
    }

    public static void notifyServiceDeleted(Service service) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateServiceDeleted(service);
        }
    }


    // MENU RELATED

    public static void notifyMenuAssigned(Service service, Menu menu) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateMenuAssigned(service, menu);
        }
    }

    public static void notifyMenuRemoved(Service service) {
        for (EventSubscriber receiver : subscribers) {
            receiver.updateMenuRemoved(service);
        }
    }
}
