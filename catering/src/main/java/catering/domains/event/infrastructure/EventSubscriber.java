package catering.domains.event.infrastructure;

import catering.domains.event.domain.Event;
import catering.domains.event.domain.Service;
import catering.domains.menu.Menu;

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