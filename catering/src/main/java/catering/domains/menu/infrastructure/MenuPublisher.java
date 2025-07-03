package catering.domains.menu.infrastructure;

import catering.domains.menu.domain.MenuItem;
import catering.domains.menu.domain.Section;
import catering.domains.menu.domain.Menu;

import java.util.ArrayList;

public class MenuPublisher {

    public static final ArrayList<MenuSubscriber> subscribers = new ArrayList<>();

    public static void addSubscriber(MenuSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public static void removeSubscriber(MenuSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public static void notifyItemDeleted(Menu menu, Section section, MenuItem menuItem) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuItemDeleted(menu, section, menuItem);
        }
    }

    public static void notifyItemDescriptionChanged(Menu menu, MenuItem menuItem) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuItemDescriptionChanged(menu, menuItem);
        }
    }

    public static void notifyItemSectionChanged(Menu menu, MenuItem menuItem, Section section) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuItemChanged(menu, section, menuItem);
        }
    }

    public static void notifySectionItemsRearranged(Menu menu, Section section) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuItemsRearranged(menu, section);
        }
    }

    public static void notifyFreeItemsRearranged(Menu menu) {
        for (MenuSubscriber er : subscribers) {
            er.updateFreeMenuItemsRearranged(menu);
        }
    }

    public static void notifySectionsRearranged(Menu menu) {
        for (MenuSubscriber er : subscribers) {
            er.updateSectionsRearranged(menu);
        }
    }

    public static void notifySectionChangedName(Menu menu, Section section) {
        for (MenuSubscriber er : subscribers) {
            er.updateSectionChangedName(menu, section);
        }
    }

    public static void notifySectionDeleted(Menu menu, Section section, boolean itemsDeleted) {
        for (MenuSubscriber er : subscribers) {
            er.updateSectionDeleted(menu, section, itemsDeleted);
        }
    }

    public static void notifyMenuDeleted(Menu m) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuDeleted(m);
        }
    }

    public static void notifyMenuPublishedState(Menu menu) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuPublishedState(menu);
        }
    }

    public static void notifyMenuTitleChanged(Menu menu) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuTitleChanged(menu);
        }
    }

    public static void notifyMenuFeaturesChanged(Menu menu) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuFeaturesChanged(menu);
        }
    }

    public static void notifyMenuItemAdded(Menu menu, MenuItem menuItem) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuItemAdded(menu, menuItem);
        }
    }

    public static void notifySectionAdded(Menu menu, Section section) {
        for (MenuSubscriber er : subscribers) {
            er.updateSectionAdded(menu, section);
        }
    }

    public static void notifyMenuCreated(Menu menu) {
        for (MenuSubscriber er : subscribers) {
            er.updateMenuCreated(menu);
        }
    }
}
