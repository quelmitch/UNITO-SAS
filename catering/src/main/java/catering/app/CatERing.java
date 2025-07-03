package catering.app;

import catering.domains.event.domain.EventManager;
import catering.domains.holidayleave.domain.HolidayLeaveManager;
import catering.domains.kitchen.domain.KitchenTaskManager;
import catering.domains.menu.MenuManager;
import catering.domains.recipe.domain.RecipeManager;
import catering.domains.shift.domain.ShiftManager;
import catering.domains.staffmember.domain.StaffMemberManager;
import catering.persistence.MenuPersistence;
import lombok.Data;

import java.util.logging.Logger;

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
        initializeLogger();
        initializeManagers();
        registerPersistenceHandlers();
    }

    private void initializeLogger() {
        Logger logger = catering.utils.LogManager.getLogger(CatERing.class);
        logger.info("Application started");
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

        menuManager.addEventReceiver(menuPersistence);
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
