package catering.domains.menu.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Objects;

@Data
public class Section {

    private int id;
    private String name;
    private ArrayList<MenuItem> menuItems;

    public Section(String name) {
        this.id = 0;
        this.name = name;
        this.menuItems = new ArrayList<>();
    }

    public Section(Section toCopy) {
        this(toCopy.name);
        for (MenuItem item : toCopy.menuItems) {
            this.menuItems.add(new MenuItem(item));
        }
    }


    // BUSINESS LOGIC - ITEMS

    public void addMenuItem(MenuItem mi) {
        menuItems.add(mi);
    }

    public void removeMenuItem(MenuItem mi) {
        menuItems.remove(mi);
    }

    public void moveMenuItem(MenuItem mi, int position) {
        menuItems.remove(mi);
        menuItems.add(position, mi);
    }

    public void updateMenuItems(ArrayList<MenuItem> newItems) {
        ArrayList<MenuItem> updated = new ArrayList<>();
        for (MenuItem mi : newItems) {
            MenuItem existing = findMenuItemById(mi.getId());
            if (existing == null) {
                updated.add(mi);
            } else {
                existing.setDescription(mi.getDescription());
                existing.setRecipe(mi.getRecipe());
                updated.add(existing);
            }
        }
        this.menuItems.clear();
        this.menuItems.addAll(updated);
    }

    private MenuItem findMenuItemById(int id) {
        for (MenuItem mi : menuItems) {
            if (mi.getId() == id)
                return mi;
        }
        return null;
    }

    public int getMenuItemPosition(MenuItem mi) {
        return menuItems.indexOf(mi);
    }

    public int getMenuItemsCount() {
        return menuItems.size();
    }


    public Section deepCopy() {
        Section copy = new Section(this.name);
        copy.id = this.id;
        for (MenuItem item : this.menuItems) {
            copy.addMenuItem(item.deepCopy());
        }
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Section [id=%d, name='%s']", id, name));
        sb.append(String.format(" - %d items", menuItems.size()));

        if (!menuItems.isEmpty()) {
            sb.append(":\n");
            for (int i = 0; i < menuItems.size(); i++) {
                sb.append(String.format("  %d. %s", (i + 1), menuItems.get(i).toString()));
                if (i < menuItems.size() - 1) sb.append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Section)) return false;
        Section other = (Section) o;

        if (this.id > 0 && other.id > 0)
            return this.id == other.id;

        return Objects.equals(this.name, other.name)
            && this.menuItems.equals(other.menuItems);
    }

    @Override
    public int hashCode() {
        if (id > 0) return Integer.hashCode(id);
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + menuItems.size();
        if (!menuItems.isEmpty()) {
            result += menuItems.get(0).hashCode();
            if (menuItems.size() > 1)
                result += menuItems.get(menuItems.size() - 1).hashCode();
        }
        return result;
    }
}
