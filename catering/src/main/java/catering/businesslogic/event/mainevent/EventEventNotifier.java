package catering.businesslogic.event.mainevent;

import catering.businesslogic.event.service.Service;
import catering.businesslogic.menu.Menu;

import java.util.ArrayList;

public class EventEventNotifier {

    private final ArrayList<EventEventReceiver> eventEventReceivers;

    public EventEventNotifier() {
        this.eventEventReceivers = new ArrayList<>();
    }

    public void addEventReceiver(EventEventReceiver receiver) {
        if (receiver != null && !eventEventReceivers.contains(receiver)) {
            eventEventReceivers.add(receiver);
        }
    }

    public void removeEventReceiver(EventEventReceiver receiver) {
        eventEventReceivers.remove(receiver);
    }

    public void notifyEventCreated(Event event) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateEventCreated(event);
        }
    }

    public void notifyEventModified(Event event) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateEventModified(event);
        }
    }

    public void notifyEventDeleted(Event event) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateEventDeleted(event);
        }
    }

    public void notifyServiceCreated(Event event, Service service) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateServiceCreated(event, service);
        }
    }

    public void notifyServiceModified(Service service) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateServiceModified(service);
        }
    }

    public void notifyServiceDeleted(Service service) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateServiceDeleted(service);
        }
    }

    public void notifyMenuAssigned(Service service, Menu menu) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateMenuAssigned(service, menu);
        }
    }

    public void notifyMenuRemoved(Service service) {
        for (EventEventReceiver receiver : eventEventReceivers) {
            receiver.updateMenuRemoved(service);
        }
    }
}
