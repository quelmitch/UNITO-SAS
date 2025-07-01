package catering.businesslogic.event.mainevent;

import catering.businesslogic.event.service.Service;
import catering.businesslogic.staffmember.StaffMember;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Event {

    private int id;
    private String name;
    private Date dateStart;
    private Date dateEnd;
    private StaffMember chef;
    private List<Service> services = new ArrayList<>();

    // CONSTRUCTORS
    public Event(String name) {
        this.name = name;
    }

    public Event(String name, Date dateStart, Date dateEnd, StaffMember chef) {
        this.name = name;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.chef = chef;
    }


    // CUSTOM GETTERS & SETTERS

    public int getChefId() {
        return chef != null ? chef.getId() : 0;
    }

    public void setChefId(int chefId) {
        // This should now be delegated to StaffMemberDAO from the outside
        throw new UnsupportedOperationException("Use StaffMemberDAO externally to load and assign a StaffMember.");
    }


    // BUSINESS LOGIC
    public void addService(Service service) {
        services.add(service);
    }

    public void removeService(Service service) {
        services.remove(service);
    }

    public boolean containsService(Service service) {
        return services.contains(service);
    }


    // UTILITY

    @Override
    public String toString() {
        return "Event [id=" + id + ", name=" + name + ", dateStart=" + dateStart +
            ", services=" + (services != null ? services.size() : 0) + "]";
    }
}
