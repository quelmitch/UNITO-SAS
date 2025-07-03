package catering.domains.menu.domain;

import catering.app.CatERing;
import catering.domains.menu.infrastructure.MenuDAO;
import catering.domains.menu.infrastructure.MenuItemDAO;
import catering.domains.menu.infrastructure.MenuPublisher;
import catering.domains.menu.infrastructure.SectionDAO;
import catering.domains.staffmember.infrastructure.AuthorizationService;
import catering.exceptions.MenuException;
import catering.exceptions.UseCaseLogicException;
import catering.domains.recipe.domain.Recipe;
import catering.domains.staffmember.domain.StaffMember;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MenuManager {
    private Menu currentMenu;

    // MENU

    public Menu createMenu() throws UseCaseLogicException {
        return this.createMenu(null);
    }

    public Menu createMenu(String title) throws UseCaseLogicException {
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();

        AuthorizationService.requireCurrentUserHasRole(StaffMember.Role.CHEF);

        Menu m = new Menu(staffMember, title);
        this.setCurrentMenu(m);

        MenuDAO.create(m);
        MenuPublisher.notifyMenuCreated(m);

        return m;
    }

    public void deleteMenu(Menu menu) throws UseCaseLogicException, MenuException {
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        AuthorizationService.requireCurrentUserHasRole(StaffMember.Role.CHEF);

        if (menu.isInUse() || !menu.isOwner(staffMember)) {
            throw new MenuException();
        }

        MenuDAO.delete(menu);
        MenuPublisher.notifyMenuDeleted(menu);
    }

    public Menu chooseMenuForCopy(Menu menuToCopy) throws UseCaseLogicException {
        AuthorizationService.requireCurrentUserHasRole(StaffMember.Role.CHEF);

        Menu copiedMenu = menuToCopy.deepCopy();
        StaffMember staffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        copiedMenu.setOwner(staffMember);

        this.setCurrentMenu(copiedMenu);

        MenuDAO.create(copiedMenu);
        MenuPublisher.notifyMenuCreated(copiedMenu);

        return copiedMenu;
    }

    public void chooseMenu(Menu menu) throws UseCaseLogicException, MenuException {
        AuthorizationService.requireCurrentUserHasRole(StaffMember.Role.CHEF);

        StaffMember currentStaffMember = CatERing.getInstance().getStaffMemberManager().getCurrentStaffMember();
        if (menu.isInUse() || !menu.isOwner(currentStaffMember)) {
            throw new MenuException();
        }
        this.currentMenu = menu;
    }

    public void changeTitle(String title) throws UseCaseLogicException {
        if (currentMenu == null)
            throw new UseCaseLogicException();
        currentMenu.setTitle(title);

        MenuDAO.saveTitle(currentMenu);
        MenuPublisher.notifyMenuTitleChanged(this.currentMenu);
    }

    public void addMenuFeatures(Menu.Feature[] features, boolean[] values) throws UseCaseLogicException {
        if (this.currentMenu == null)
            throw new UseCaseLogicException();
        if (features.length != values.length)
            throw new UseCaseLogicException();
        for (int i = 0; i < features.length; i++) {
            this.currentMenu.setFeature(features[i], values[i]);
        }

        MenuDAO.saveFeatures(this.currentMenu);
        MenuPublisher.notifyMenuFeaturesChanged(this.currentMenu);
    }

    public void publish() throws UseCaseLogicException {
        if (currentMenu == null)
            throw new UseCaseLogicException();
        currentMenu.setPublished(true);

        MenuDAO.savePublished(this.currentMenu);
        MenuPublisher.notifyMenuPublishedState(this.currentMenu);
    }


    // SECTION

    public Section defineSection(String name) throws UseCaseLogicException {

        if (currentMenu == null)
            throw new UseCaseLogicException();

        Section newSection = this.currentMenu.addSection(name);

        SectionDAO.create(this.currentMenu.getId(), newSection, this.currentMenu.getSectionPosition(newSection));
        MenuPublisher.notifySectionAdded(this.currentMenu, newSection);

        return newSection;
    }

    public void deleteSection(Section section, boolean deleteItems) throws UseCaseLogicException {
        if (currentMenu == null || currentMenu.getSectionPosition(section) < 0)
            throw new UseCaseLogicException();
        this.currentMenu.removeSection(section, deleteItems);

        SectionDAO.delete(this.currentMenu.getId(), section);
        if (!deleteItems)
            MenuItemDAO.create(this.currentMenu.getId(), 0, section.getMenuItems());
        MenuPublisher.notifySectionDeleted(this.currentMenu, section, deleteItems);
    }

    public void changeSectionName(Section section, String name) throws UseCaseLogicException {
        if (currentMenu == null || currentMenu.getSectionPosition(section) < 0)
            throw new UseCaseLogicException();
        section.setName(name);

        SectionDAO.saveName(section);
        MenuPublisher.notifySectionChangedName(this.currentMenu, section);
    }

    public void moveSection(Section sec, int position) throws UseCaseLogicException {
        if (currentMenu == null || currentMenu.getSectionPosition(sec) < 0)
            throw new UseCaseLogicException();
        if (position < 0 || position >= currentMenu.getSectionCount())
            throw new IllegalArgumentException();
        this.currentMenu.moveSection(sec, position);

        MenuDAO.saveSectionOrder(this.currentMenu);
        MenuPublisher.notifySectionsRearranged(this.currentMenu);
    }


    // MENU ITEM

    public MenuItem insertItem(Recipe recipe, Section section, String description) throws UseCaseLogicException {
        if (this.currentMenu == null)
            throw new UseCaseLogicException();
        if (section != null && this.currentMenu.getSectionPosition(section) < 0)
            throw new UseCaseLogicException();
        MenuItem menuItem = this.currentMenu.addItem(recipe, section, description);

        int sectionId = section == null ? 0 : section.getId();
        int position = section == null ? this.currentMenu.getFreeItemPosition(menuItem) : section.getMenuItemPosition(menuItem);
        MenuItemDAO.create(this.currentMenu.getId(), sectionId, menuItem, position);
        MenuPublisher.notifyMenuItemAdded(this.currentMenu, menuItem);

        return menuItem;
    }

    public MenuItem insertItem(Recipe recipe, Section section) throws UseCaseLogicException {
        return this.insertItem(recipe, section, recipe.getName());
    }

    public MenuItem insertItem(Recipe recipe) throws UseCaseLogicException {
        return this.insertItem(recipe, null, recipe.getName());
    }

    public MenuItem insertItem(Recipe recipe, String description) throws UseCaseLogicException {
        return this.insertItem(recipe, null, description);
    }

    public void moveMenuItem(MenuItem menuItem, int position) throws UseCaseLogicException {
        this.moveMenuItem(menuItem, null, position);
    }

    public void moveMenuItem(MenuItem menuItem, Section section, int position) throws UseCaseLogicException {
        if (section == null) {
            if (currentMenu == null || currentMenu.getFreeItemPosition(menuItem) < 0)
                throw new UseCaseLogicException();
            if (position < 0 || position >= currentMenu.getFreeItemCount())
                throw new IllegalArgumentException();
            currentMenu.moveFreeItem(menuItem, position);

            MenuDAO.saveFreeItemOrder(this.currentMenu);
            MenuPublisher.notifyFreeItemsRearranged(this.currentMenu);
        } else {
            if (currentMenu == null || currentMenu.getSectionPosition(section) < 0 || section.getMenuItemPosition(menuItem) < 0)
                throw new UseCaseLogicException();
            if (position < 0 || position >= section.getMenuItemsCount())
                throw new IllegalArgumentException();
            section.moveMenuItem(menuItem, position);

            SectionDAO.saveItemOrder(section);
            MenuPublisher.notifySectionItemsRearranged(this.currentMenu, section);
        }
    }

    public void editMenuItemDescription(MenuItem menuItem, String description) throws UseCaseLogicException {
        if (currentMenu == null)
            throw new UseCaseLogicException();
        if (currentMenu.getSection(menuItem) == null && currentMenu.getFreeItemPosition(menuItem) < 0)
            throw new UseCaseLogicException();

        menuItem.setDescription(description);

        MenuItemDAO.updateDescription(menuItem);
        MenuPublisher.notifyItemDescriptionChanged(this.currentMenu, menuItem);
    }

    public void deleteItem(MenuItem menuItem) throws UseCaseLogicException {

        if (currentMenu == null)
            throw new UseCaseLogicException();
        Section section = null;
        try {
            section = currentMenu.getSection(menuItem);
        } catch (IllegalArgumentException ex) {

            throw new UseCaseLogicException();
        }
        currentMenu.removeItem(menuItem);

        MenuItemDAO.delete(menuItem);
        if (section != null) {
            SectionDAO.saveItemOrder(section);
        } else {
            MenuDAO.saveFreeItemOrder(this.currentMenu);
        }
        MenuPublisher.notifyItemDeleted(this.currentMenu, section, menuItem);
    }

    public void assignItemToSection(MenuItem menuItem, Section section) throws UseCaseLogicException {

        if (currentMenu == null)
            throw new UseCaseLogicException();

        if (section != null && currentMenu.getSectionPosition(section) < 0)
            throw new UseCaseLogicException();

        Section oldSection = currentMenu.getSection(menuItem);
        if (oldSection == null && currentMenu.getFreeItemPosition(menuItem) < 0)
            throw new UseCaseLogicException();

        if (section == oldSection)
            return;

        this.currentMenu.changeItemSection(menuItem, oldSection, section);

        int sectionId = section == null ? 0 : section.getId();
        MenuItemDAO.updateSection(sectionId, menuItem);
        MenuPublisher.notifyItemSectionChanged(this.currentMenu, menuItem, section);
    }

}
