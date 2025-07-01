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
    private static StaffMember nonOrganizer;
    private static StaffMember owner;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
        LOGGER.info("Starting test: HolidayLeaveTest");
    }

    @BeforeEach
    void setup() {
        organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        nonOrganizer = StaffMemberDAO.loadByEmail("luca.verdi@example.com");
        owner = StaffMemberDAO.loadByEmail("chiara.bruni@example.com");

        assertNotNull(organizer);
        assertNotNull(nonOrganizer);
        assertNotNull(owner);

        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE));
        assertFalse(nonOrganizer.hasRole(StaffMember.Role.ORGANIZZATORE));
        assertTrue(owner.hasRole(StaffMember.Role.PROPRIETARIO));
    }

    @Test
    void requestHolidayLeaveByAnyStaffMember() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(nonOrganizer.getEmail());

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
    void approveHolidayLeaveByOrganizer() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(nonOrganizer.getEmail());
        HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                DateUtils.safeValueOf("2025-09-01"),
                DateUtils.safeValueOf("2025-09-05")
        );

        app.getHolidayLeaveManager().fakeLogin(organizer.getEmail());
        app.getHolidayLeaveManager().approveLeave(leave);

        assertEquals(HolidayLeave.RequestStatus.ACCETTATA, leave.getStatus());

        HolidayLeave reloaded = HolidayLeaveDAO.loadById(leave.getId());
        assertEquals(HolidayLeave.RequestStatus.ACCETTATA, reloaded != null ? reloaded.getStatus() : null);
    }

    @Test
    void rejectHolidayLeaveByOwner() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(nonOrganizer.getEmail());
        HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                DateUtils.safeValueOf("2025-10-01"),
                DateUtils.safeValueOf("2025-10-05")
        );

        app.getHolidayLeaveManager().fakeLogin(owner.getEmail());
        app.getHolidayLeaveManager().rejectLeave(leave);

        assertEquals(HolidayLeave.RequestStatus.RIFIUTATA, leave.getStatus());

        HolidayLeave reloaded = HolidayLeaveDAO.loadById(leave.getId());
        assertEquals(HolidayLeave.RequestStatus.RIFIUTATA, reloaded != null ? reloaded.getStatus() : null);
    }

    @Test
    void unauthorizedApprovalShouldFail() {
        assertThrows(UseCaseLogicException.class, () -> {
            app.getHolidayLeaveManager().fakeLogin(nonOrganizer.getEmail());
            HolidayLeave leave = app.getHolidayLeaveManager().requestHolidayLeave(
                    DateUtils.safeValueOf("2025-11-01"),
                    DateUtils.safeValueOf("2025-11-05")
            );
            app.getHolidayLeaveManager().approveLeave(leave);
        });
    }

    @Test
    void deleteHolidayLeaveByRequester() throws UseCaseLogicException {
        app.getHolidayLeaveManager().fakeLogin(nonOrganizer.getEmail());
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
