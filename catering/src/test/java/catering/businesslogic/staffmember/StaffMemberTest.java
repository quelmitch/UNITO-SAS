package catering.businesslogic.staffmember;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.kitchen.SummarySheetTest;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import catering.util.DateUtils;

import java.util.Date;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class StaffMemberTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffMemberTest.class);

    private static CatERing app;
    private static StaffMember organizer;
    private static StaffMember notOrganizer;
    private static StaffMember owner;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {

        // Set up the organizer user
        organizer = StaffMember.load("giovanni.ricci@example.com");
        assertNotNull(organizer, "'organizer' user should be loaded");
        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should have organizer role");

        // Set up the notOrganizer user
        notOrganizer = StaffMember.load("luca.verdi@example.com");
        assertNotNull(notOrganizer, "'notOrganizer' user should be loaded");
        assertFalse(notOrganizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should not have organizer role");

        // Set up the owner user
        owner = StaffMember.load("chiara.bruni@example.com");
        assertNotNull(owner, "'owner' user should be loaded");
        assertTrue(owner.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should have organizer role");
        assertTrue(owner.hasRole(StaffMember.Role.PROPRIETARIO), "Staff Member should have owner role");
    }

    @Test
    @Order(1)
    void createOccasionalStaffMemberProfileByOrganizer() {
        LOGGER.info("Adding New Occasional Staff Member Profile by Organizer");

        try {
            app.getStaffMemberManager().fakeLogin(organizer.getEmail());

            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                "paolo.nesi@example.com",
                "Paolo",
                "Nesi",
                DateUtils.safeValueOf("1985-07-12"),
                "Via Savona 10, Torino",
                "+39 338 1882136",
                700,
                StaffMember.EmploymentType.OCCASIONALE
            );

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void createPermanentStaffMemberProfileByOrganizer() {
        LOGGER.info("Adding New Permament Staff Member Profile by Organizer");

        assertThrows(UseCaseLogicException.class, () -> {
            app.getStaffMemberManager().fakeLogin(organizer.getEmail());

            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                "paolo.nesi@example.com",
                "Paolo",
                "Nesi",
                DateUtils.safeValueOf("1985-07-12"),
                "Via Savona 10, Torino",
                "+39 338 1882136",
                700,
                StaffMember.EmploymentType.PERMANENTE
            );

        });
    }

    @Test
    @Order(2)
    void updateToPermanentStaffMemberProfileByOrganizer() {
        LOGGER.info("Updating EmploymentType from Occasional to Permanent by Organizer");

        assertThrows(UseCaseLogicException.class, () -> {
            app.getStaffMemberManager().fakeLogin(organizer.getEmail());

            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                "paolo.nesi@example.com",
                "Paolo",
                "Nesi",
                DateUtils.safeValueOf("1985-07-12"),
                "Via Savona 10, Torino",
                "+39 338 1882136",
                700,
                StaffMember.EmploymentType.OCCASIONALE
            );

            app.getStaffMemberManager().changeEmploymentType(newMember, StaffMember.EmploymentType.PERMANENTE);

        });
    }
}
