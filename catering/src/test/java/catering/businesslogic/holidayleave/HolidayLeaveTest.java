package catering.businesslogic.holidayleave;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staffmember.StaffMember;
import catering.businesslogic.staffmember.StaffMemberDAO;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class HolidayLeaveTest {
    private static final Logger LOGGER = LogManager.getLogger(HolidayLeaveTest.class);

    private static CatERing app;
    private static HolidayLeave holidayLeave;
    private static StaffMember organizer;
    private static StaffMember nonOrganizer;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();

        LOGGER.info("Starting test: HolidayLeaveTest");
    }

    @BeforeEach
    void setup() {
        organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        assertNotNull(organizer, "'organizer' user should be loaded");
        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should have organizer role");

        nonOrganizer = StaffMemberDAO.loadByEmail("luca.verdi@example.com");
        assertNotNull(nonOrganizer, "'notOrganizer' user should be loaded");
        assertFalse(nonOrganizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should not have organizer role");

        // TODO: create the HolidayLeave
    }

    @Test
    void updateHolidayLeaveAsOrganizer() throws UseCaseLogicException {
        // TODO:
    }

    @Test
    void updateHolidayLeaveAsNonOrganizer() throws UseCaseLogicException {
        // TODO:
    }
}
