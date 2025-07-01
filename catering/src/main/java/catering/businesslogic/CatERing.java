package catering.businesslogic;

import catering.businesslogic.event.EventManager;
import catering.businesslogic.holidayleave.HolidayLeaveEventNotifier;
import catering.businesslogic.holidayleave.HolidayLeaveManager;
import catering.businesslogic.holidayleave.HolidayLeavePersistence;
import catering.businesslogic.kitchen.KitchenTaskManager;
import catering.businesslogic.menu.MenuManager;
import catering.businesslogic.recipe.RecipeManager;
import catering.businesslogic.shift.ShiftManager;
import catering.businesslogic.staffmember.StaffMemberEventNotifier;
import catering.businesslogic.staffmember.StaffMemberManager;
import catering.persistence.KitchenTaskPersistence;
import catering.persistence.MenuPersistence;
import catering.businesslogic.staffmember.StaffMemberPersistence;
import lombok.Data;
import lombok.Getter;

@Data
public class CatERing {
    private static CatERing singleInstance;

    public static CatERing getInstance() {
        if (singleInstance == null) {
            singleInstance = new CatERing();
        }
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
        menuManager = new MenuManager();
        recipeManager = new RecipeManager();
        staffMemberManager = new StaffMemberManager();
        eventManager = new EventManager();
        kitchenTaskManager = new KitchenTaskManager();
        shiftManager = new ShiftManager();
        holidayLeaveManager = new HolidayLeaveManager();

        MenuPersistence menuPersistence = new MenuPersistence();
        KitchenTaskPersistence kitchenTaskPersistence = new KitchenTaskPersistence();

        menuManager.addEventReceiver(menuPersistence);
        kitchenTaskManager.addEventReceiver(kitchenTaskPersistence);
        StaffMemberEventNotifier.registerReceiver(new StaffMemberPersistence());
        HolidayLeaveEventNotifier.registerReceiver(new HolidayLeavePersistence());
    }

    public static void main(String[] args) {
        // Get the singleton instance which initializes all managers
        CatERing app = CatERing.getInstance();

        System.out.println("CatERing application initialized successfully.");

        // Log which managers are available
        System.out.println("Available managers:");
        System.out.println("- Menu Manager: " + (app.getMenuManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Recipe Manager: " + (app.getRecipeManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Staff Member Manager: " + (app.getStaffMemberManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Event Manager: " + (app.getEventManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Kitchen Task Manager: " + (app.getKitchenTaskManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Shift Manager: " + (app.getShiftManager() != null ? "OK" : "NOT AVAILABLE"));
    }
}
