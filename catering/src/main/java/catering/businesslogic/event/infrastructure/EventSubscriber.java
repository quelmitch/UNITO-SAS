package catering.businesslogic.event.infrastructure;

import catering.businesslogic.event.domain.Event;
import catering.businesslogic.event.domain.Service;
import catering.businesslogic.menu.Menu;

public interface EventSubscriber {

    void updateEventCreated(Event event);

    void updateEventModified(Event event);

    void updateEventDeleted(Event event);

    void updateServiceCreated(Event event, Service service);

    void updateServiceModified(Service service);

    void updateServiceDeleted(Service service);

    void updateMenuAssigned(Service service, Menu menu);

    void updateMenuRemoved(Service service);
}