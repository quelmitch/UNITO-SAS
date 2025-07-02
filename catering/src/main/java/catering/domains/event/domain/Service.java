package catering.domains.event.domain;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Objects;

import catering.domains.menu.Menu;
import catering.domains.menu.MenuItem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Service {
    private int id;
    private String name;
    private Date date;
    private Time timeStart;
    private Time timeEnd;
    private String location;
    private int eventId;
    private Menu menu;

    public Service(String name) {
        this.name = name;
    }

    public Service(String name, Date date, Time timeStart, Time timeEnd, String location, int eventId) {
        this.name = name;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.location = location;
        this.eventId = eventId;
    }

    public int getMenuId() {
        return (menu != null) ? menu.getId() : 0;
    }

    public ArrayList<MenuItem> getMenuItems() {
        return (menu != null) ? menu.getItems() : new ArrayList<>();
    }


    // BUSINESS LOGIC

    public void assignMenuToService(Menu menu) {
        this.menu = menu;
    }

    public void removeMenu() {
        this.menu = null;
    }


    // UTILITY

    @Override
    public String toString() {
        return "Service [id=" + id + ", name=" + name + ", date=" + date + ", location=" + location +
            ", menu=" + (menu != null ? menu.getTitle() : "none") + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Service)) return false;
        Service other = (Service) obj;

        return id > 0 && other.id > 0 ? id == other.id :
            Objects.equals(name, other.name) &&
                Objects.equals(date, other.date) &&
                Objects.equals(timeStart, other.timeStart) &&
                Objects.equals(timeEnd, other.timeEnd) &&
                Objects.equals(location, other.location) &&
                Objects.equals(menu, other.menu) &&
                (eventId <= 0 || other.eventId <= 0 || eventId == other.eventId);
    }
}
