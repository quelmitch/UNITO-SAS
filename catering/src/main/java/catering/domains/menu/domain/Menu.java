package catering.domains.menu.domain;

import java.util.*;

import catering.domains.recipe.domain.KitchenProcess;
import catering.domains.recipe.domain.Recipe;
import catering.domains.staffmember.domain.StaffMember;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Menu {

    public enum Feature {
        NEEDS_COOK,
        FINGER_FOOD,
        BUFFET,
        WARM_DISHES,
        NEEDS_KITCHEN
    }

    private static final EnumSet<Feature> DEFAULT_FEATURES = EnumSet.allOf(Feature.class);

    private int id;
    private String title;
    private boolean published;
    private boolean inUse;
    private EnumMap<Feature, Boolean> features;
    private ArrayList<MenuItem> freeItems;
    private ArrayList<Section> sections;
    private StaffMember owner;

    public Menu(StaffMember owner, String title, EnumSet<Feature> menuFeatures) {
        this.id = 0;
        this.title = title;
        this.owner = owner;
        this.published = false;
        this.inUse = false;
        this.features = new EnumMap<>(Feature.class);
        this.sections = new ArrayList<>();
        this.freeItems = new ArrayList<>();

        for (Feature feature : menuFeatures) {
            this.features.put(feature, false);
        }
    }

    public Menu(StaffMember owner, String title) {
        this(owner, title, DEFAULT_FEATURES);
    }


    public boolean isOwner(StaffMember u) {
        return u.getId() == this.owner.getId();
    }


    // SECTION MANAGEMENT
    public Section addSection(String name) {
        Section sec = new Section(name);
        this.sections.add(sec);
        return sec;
    }

    public Section getSection(int position) {
        if (position < 0 || position >= sections.size()) {
            throw new IndexOutOfBoundsException("Invalid section position");
        }
        return this.sections.get(position);
    }

    public Section getSectionById(int id) {
        for (Section sec : sections) {
            if (sec.getId() == id) {
                return sec;
            }
        }
        return null;
    }

    public Section getSection(String name) {
        for (Section sec : sections) {
            if (sec.getName().equals(name)) {
                return sec;
            }
        }
        return null;
    }

    public Section getSection(MenuItem mi) {
        for (Section sec : sections) {
            if (sec.getMenuItemPosition(mi) >= 0) {
                return sec;
            }
        }
        if (freeItems.contains(mi)) {
            return null;
        }
        throw new IllegalArgumentException("MenuItem not found in this menu");
    }

    public boolean hasSection(Section sec) {
        return this.sections.contains(sec);
    }

    public int getSectionPosition(Section sec) {
        return this.sections.indexOf(sec);
    }

    public void moveSection(Section sec, int position) {
        sections.remove(sec);
        sections.add(position, sec);
    }

    public void removeSection(Section s, boolean deleteItems) {
        if (!deleteItems) {
            this.freeItems.addAll(s.getMenuItems());
        }
        this.sections.remove(s);
    }

    public int getSectionCount() {
        return sections.size();
    }


    // ITEM MANAGEMENT

    public MenuItem addItem(Recipe recipe, Section sec, String desc) {
        MenuItem mi = new MenuItem(recipe, desc);
        if (sec != null) {
            sec.addMenuItem(mi);
        } else {
            this.freeItems.add(mi);
        }
        return mi;
    }

    public int getFreeItemPosition(MenuItem mi) {
        return freeItems.indexOf(mi);
    }

    public int getFreeItemCount() {
        return freeItems.size();
    }

    public void moveFreeItem(MenuItem mi, int position) {
        this.freeItems.remove(mi);
        this.freeItems.add(position, mi);
    }

    public void changeItemSection(MenuItem mi, Section oldSec, Section newSec) {
        if (oldSec == null) {
            freeItems.remove(mi);
        } else {
            oldSec.removeMenuItem(mi);
        }

        if (newSec == null) {
            freeItems.add(mi);
        } else {
            newSec.addMenuItem(mi);
        }
    }

    public void removeItem(MenuItem mi) {
        Section sec = getSection(mi);
        if (sec == null) {
            freeItems.remove(mi);
        } else {
            sec.removeMenuItem(mi);
        }
    }

    public void updateFreeItems(ArrayList<MenuItem> newItems) {
        ArrayList<MenuItem> updatedList = new ArrayList<>();
        for (MenuItem mi : newItems) {
            MenuItem prev = findItemById(mi.getId());
            if (prev == null) {
                updatedList.add(mi);
            } else {
                prev.setDescription(mi.getDescription());
                prev.setRecipe(mi.getRecipe());
                updatedList.add(prev);
            }
        }
        this.freeItems.clear();
        this.freeItems.addAll(updatedList);
    }

    public ArrayList<MenuItem> getItems() {
        ArrayList<MenuItem> allItems = new ArrayList<>(this.freeItems);
        for (Section section : this.sections) {
            allItems.addAll(section.getMenuItems());
        }
        return allItems;
    }


    // KITCHEN PROCESS MANAGEMENT

    public ArrayList<KitchenProcess> getKitchenProcesses() {
        ArrayList<KitchenProcess> allKitchenProcesses = new ArrayList<>();
        for (MenuItem item : this.getItems()) {
            Recipe recipe = item.getRecipe();
            allKitchenProcesses.add(recipe);
            allKitchenProcesses.addAll(recipe.getPreparations());
        }
        return allKitchenProcesses;
    }


    // FEATURE MANAGEMENT

    public void initializeDefaultFeatures() {
        for (Feature feature : DEFAULT_FEATURES) {
            features.putIfAbsent(feature, false);
        }
    }

    public Map<Feature, Boolean> getFeatures() {
        return this.features;
    }

    public boolean getFeature(Feature feature) {
        return this.features.getOrDefault(feature, false);
    }

    public void setFeature(Feature feature, boolean value) {
        if (this.features.containsKey(feature)) {
            this.features.put(feature, value);
        }
    }

    public boolean requiresKitchenPreparation() {
        return getFeature(Feature.NEEDS_KITCHEN)
            || getFeature(Feature.NEEDS_COOK)
            || getFeature(Feature.WARM_DISHES);
    }


    //

    private MenuItem findItemById(int id) {
        for (MenuItem mi : freeItems) {
            if (mi.getId() == id) {
                return mi;
            }
        }
        return null;
    }


    public Menu deepCopy() {
        Menu copy = new Menu(this.owner, this.title, DEFAULT_FEATURES);
        copy.published = this.published;
        copy.inUse = this.inUse;
        copy.features.putAll(this.features);

        for (Section sec : this.sections)
            copy.sections.add(sec.deepCopy());

        for (MenuItem mi : this.freeItems)
            copy.freeItems.add(mi.deepCopy());

        return copy;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.title)
            .append(" (autore: ")
            .append(this.owner != null ? this.owner.getEmail() : "unknown")
            .append("), ")
            .append(published ? "pubblicato" : "non pubblicato")
            .append(", ")
            .append(inUse ? "in uso" : "non in uso");

        for (Map.Entry<Feature, Boolean> entry : features.entrySet()) {
            result.append("\n").append(entry.getKey()).append(": ").append(entry.getValue());
        }

        if (!sections.isEmpty()) {
            result.append("\n\nSections:");
            for (Section sec : sections) {
                result.append("\n").append(sec.toString());
            }
        }

        if (!freeItems.isEmpty()) {
            result.append("\n\nVOCI LIBERE:");
            for (MenuItem mi : freeItems) {
                result.append("\n\t").append(mi.toString());
            }
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Menu other = (Menu) obj;

        return id == other.id &&
            published == other.published &&
            inUse == other.inUse &&
            (Objects.equals(title, other.title)) &&
            (Objects.equals(owner, other.owner)) &&
            (Objects.equals(features, other.features)) &&
            (Objects.equals(freeItems, other.freeItems)) &&
            (Objects.equals(sections, other.sections));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + Boolean.hashCode(published);
        result = 31 * result + Boolean.hashCode(inUse);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (features != null ? features.hashCode() : 0);
        result = 31 * result + (freeItems != null ? freeItems.hashCode() : 0);
        result = 31 * result + (sections != null ? sections.hashCode() : 0);
        return result;
    }

}
