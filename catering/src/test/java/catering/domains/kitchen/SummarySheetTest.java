package catering.domains.kitchen;

import static org.junit.jupiter.api.Assertions.*;

import catering.domains.event.infrastructure.EventDAO;
import catering.domains.event.infrastructure.ServiceDAO;
import catering.domains.kitchen.domain.Assignment;
import catering.domains.kitchen.domain.KitchenTask;
import catering.domains.kitchen.domain.SummarySheet;
import catering.domains.staffmember.domain.StaffMember;
import catering.domains.staffmember.StaffMemberCreationTest;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.Date;
import java.sql.Time;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.app.CatERing;
import catering.exceptions.UseCaseLogicException;
import catering.domains.event.domain.Event;
import catering.domains.event.domain.Service;
import catering.domains.shift.domain.Shift;
import catering.persistence.PersistenceManager;
import catering.utils.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class SummarySheetTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffMemberCreationTest.class);

    private static CatERing app;
    private static StaffMember chef;
    private static StaffMember cook;
    private static Event testEvent;
    private static Service testService;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {

        try {
            // Set up the chef user
            chef = StaffMemberDAO.loadByEmail("antonio.neri@example.com");
            assertNotNull(chef, "Chef user should be loaded");
            assertTrue(chef.hasRole(StaffMember.Role.CHEF), "Staff Member should have chef role");

            // Set up the cook user
            cook = StaffMemberDAO.loadByEmail("luca.verdi@example.com");
            assertNotNull(cook, "Cook user should be loaded");

            // Set up event and service
            testEvent = EventDAO.loadByName("Gala Aziendale Annuale");
            assertNotNull(testEvent, "Test event should be loaded");

            testService = ServiceDAO.loadByName("Pranzo Buffet Aziendale");
            assertNotNull(testService, "Test service should be loaded");

            // Login as chef
            app.getStaffMemberManager().fakeLogin(chef.getEmail());

            assertEquals(chef, app.getStaffMemberManager().getCurrentStaffMember(), "Current user should be the chef");

        } catch (UseCaseLogicException e) {
            LOGGER.severe(e.getMessage());
        }

    }

    @Test
    @Order(1)
    void testSummarySheetCreation() {
        LOGGER.info("Testing summary sheet creation");

        try {
            // Create summary sheet
            SummarySheet sheet = app.getKitchenTaskManager().generateSummarySheet(testEvent, testService);

            // Verify summary sheet was created properly
            assertNotNull(sheet, "Summary sheet should not be null");
            assertEquals(chef, sheet.getOwner(), "Sheet owner should be the chef who created it");
            assertNotNull(sheet.getTasks(), "Task list should not be null");
            assertFalse(sheet.getTasks().isEmpty(), "Task list should contain tasks");

            LOGGER.info("Created summary sheet: " + sheet.toString());
        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testTaskAssignment() {
        LOGGER.info("Testing task assignment to cook");

        try {
            // Create summary sheet
            SummarySheet sheet = app.getKitchenTaskManager().generateSummarySheet(testEvent, testService);
            assertNotNull(sheet, "Summary sheet should not be null");
            assertFalse(sheet.getTasks().isEmpty(), "Task list should contain tasks");

            // Get the first task
            KitchenTask taskToAssign = sheet.getTasks().get(0);
            assertNotNull(taskToAssign, "Task to assign should not be null");

            // Create a shift
            Date shiftDate = Date.valueOf("2025-04-07");
            Time startTime = Time.valueOf("09:00:00");
            Time endTime = Time.valueOf("14:00:00");
            Shift shift = new Shift(shiftDate, startTime, endTime);
            shift.addBooking(cook);

            // Assign the task
            Assignment assignment = app.getKitchenTaskManager().assignTask(taskToAssign, shift, cook);

            // Verify assignment
            assertNotNull(assignment, "Assignment should not be null");
            assertEquals(taskToAssign, assignment.getTask(), "Assignment should reference the correct task");
            assertEquals(cook, assignment.getCook(), "Assignment should reference the correct cook");
            assertEquals(shift, assignment.getShift(), "Assignment should reference the correct shift");

            // Verify assignment is in sheet's assignment list
            assertTrue(sheet.getAssignments().contains(assignment),
                    "Sheet's assignment list should contain the new assignment");

            LOGGER.info("Successfully assigned task. Assignment details: " +
                    "Task: " + assignment.getTask().getDescription() +
                    ", Cook: " + assignment.getCook().getEmail() +
                    ", Shift: " + assignment.getShift().getDate() + " " +
                    assignment.getShift().getStartTime() + "-" +
                    assignment.getShift().getEndTime());

            LOGGER.info(sheet.toString());
        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
