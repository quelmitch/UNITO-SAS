package catering.businesslogic.staffmember;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.persistence.PersistenceManager;
import catering.util.DateUtils;
import catering.util.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StaffMemberDeletionTest {
    private static final Logger LOGGER = LogManager.getLogger(StaffMemberCreationTest.class);

    private static CatERing app;
    private static StaffMember organizer;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();

        LOGGER.info("Starting test: StaffMemberDeletionTest");
    }

    @BeforeEach
    void setup() {
        organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        assertNotNull(organizer, "'organizer' user should be loaded");
        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should have organizer role");
    }

    @Test
    void deleteExistingStaffMember() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(organizer.getEmail());

        StaffMember staffToDelete = app.getStaffMemberManager().addNewStaffMember(
            "delete.me@example.com",
            "Delete",
            "Me",
            DateUtils.safeValueOf("1990-01-01"),
            "Via Example 1, Torino",
            "+39 300 0000000",
            500,
            StaffMember.EmploymentType.OCCASIONALE
        );
        assertNotNull(staffToDelete);

        boolean deleted = app.getStaffMemberManager().deleteStaffMember(staffToDelete);
        assertTrue(deleted, "Staff member should be deleted successfully");

        StaffMember loadedAfterDelete = StaffMemberDAO.loadByEmail("delete.me@example.com");
        assertNull(loadedAfterDelete, "Deleted staff member should no longer be found in DB");
    }
}
