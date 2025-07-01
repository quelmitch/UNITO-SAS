package catering.businesslogic;

import catering.businesslogic.event.EventManager;
import catering.businesslogic.holidayleave.HolidayLeaveEventNotifier;
import catering.businesslogic.holidayleave.HolidayLeaveManager;
import catering.businesslogic.holidayleave.HolidayLeavePersistence;
import catering.businesslogic.kitchen.KitchenTaskManager;
import catering.businesslogic.menu.MenuManager;
import catering.businesslogic.recipe.RecipeManager;
import catering.businesslogic.shift.ShiftManager;
import catering.businesslogic.staffmember.StaffMemberManager;
import catering.persistence.KitchenTaskPersistence;
import catering.persistence.MenuPersistence;
import lombok.Data;

@Data
public class CatERing {
    private static CatERing singleInstance;

    public static CatERing getInstance() {
        if (singleInstance == null)
            singleInstance = new CatERing();
        return singleInstance;
    }

    private MenuManager menuManager;
    private RecipeManager recipeManager;
    private StaffMemberManager staffMemberManager;
    private EventManager eventManager;
    private KitchenTaskManager kitchenTaskManager;
    private ShiftManager shiftManager;
    private HolidayLeaveManager holidayLeaveManager;

    private CatERing() {
        initializeManagers();
        registerPersistenceHandlers();
    }

    private void initializeManagers() {
        menuManager = new MenuManager();
        recipeManager = new RecipeManager();
        staffMemberManager = new StaffMemberManager();
        eventManager = new EventManager();
        kitchenTaskManager = new KitchenTaskManager();
        shiftManager = new ShiftManager();
        holidayLeaveManager = new HolidayLeaveManager();
    }

    private void registerPersistenceHandlers() {
        MenuPersistence menuPersistence = new MenuPersistence();
        KitchenTaskPersistence kitchenTaskPersistence = new KitchenTaskPersistence();

        menuManager.addEventReceiver(menuPersistence);
        kitchenTaskManager.addEventReceiver(kitchenTaskPersistence);
        HolidayLeaveEventNotifier.registerReceiver(new HolidayLeavePersistence());
    }

    public static void main(String[] args) {
        CatERing app = CatERing.getInstance();
        System.out.println("CatERing application initialized successfully.");

        checkManager("Menu Manager", app.getMenuManager());
        checkManager("Recipe Manager", app.getRecipeManager());
        checkManager("Staff Member Manager", app.getStaffMemberManager());
        checkManager("Event Manager", app.getEventManager());
        checkManager("Kitchen Task Manager", app.getKitchenTaskManager());
        checkManager("Shift Manager", app.getShiftManager());
    }

    private static void checkManager(String managerName, Object manager) {
        String status = (manager != null) ? "OK" : "NOT AVAILABLE";
        System.out.println("- " + managerName + ": " + status);
    }
}
