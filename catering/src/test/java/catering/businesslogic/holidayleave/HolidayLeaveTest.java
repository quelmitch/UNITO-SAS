package catering.businesslogic.holidayleave;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staffmember.StaffMember;
import catering.businesslogic.staffmember.StaffMemberDAO;
import catering.persistence.PersistenceManager;
import catering.util.DateUtils;
import catering.util.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class HolidayLeaveTest {
    private static final Logger LOGGER = LogManager.getLogger(HolidayLeaveTest.class);

    private static CatERing app;
    private static StaffMember organizer;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();

        LOGGER.info("Starting test: HolidayLeaveTest");
    }

    @BeforeEach
    void setup() {
        organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        assertNotNull(organizer);
        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE));
    }

    @Test
    void requestHolidayLeaveByOrganizer() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(organizer.getEmail());

        HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                DateUtils.safeValueOf("2025-08-01"),
                DateUtils.safeValueOf("2025-08-10")
        );

        assertNotNull(leave);
        assertEquals(HolidayLeave.RequestStatus.IN_ATTESA, leave.getStatus());

        HolidayLeave loaded = HolidayLeaveDAO.loadById(leave.getId());
        assertNotNull(loaded);
        assertEquals(leave.getStartDate(), loaded.getStartDate());
    }

    @Test
    void approveHolidayLeave() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(organizer.getEmail());

        HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                DateUtils.safeValueOf("2025-09-01"),
                DateUtils.safeValueOf("2025-09-05")
        );

        app.getHolidayLeaveManager().approveLeave(leave);
        assertEquals(HolidayLeave.RequestStatus.ACCETTATA, leave.getStatus());

        HolidayLeave reloaded = HolidayLeaveDAO.loadById(leave.getId());
        assertEquals(HolidayLeave.RequestStatus.ACCETTATA, reloaded.getStatus());
    }

    @Test
    void denyHolidayLeave() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(organizer.getEmail());

        HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                DateUtils.safeValueOf("2025-10-01"),
                DateUtils.safeValueOf("2025-10-05")
        );

        app.getHolidayLeaveManager().rejectLeave(leave);
        assertEquals(HolidayLeave.RequestStatus.RIFIUTATA, leave.getStatus());

        HolidayLeave reloaded = HolidayLeaveDAO.loadById(leave.getId());
        assertEquals(HolidayLeave.RequestStatus.RIFIUTATA, reloaded.getStatus());
    }

    @Test
    void deleteHolidayLeave() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(organizer.getEmail());

        HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                DateUtils.safeValueOf("2025-12-01"),
                DateUtils.safeValueOf("2025-12-10")
        );

        boolean deleted = app.getHolidayLeaveManager().cancelLeave(leave);
        assertTrue(deleted);

        HolidayLeave reloaded = HolidayLeaveDAO.loadById(leave.getId());
        assertNull(reloaded);
    }
}
