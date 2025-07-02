package catering.domains.staffmember;

import catering.app.CatERing;
import catering.domains.staffmember.domain.StaffMember;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.exceptions.UseCaseLogicException;
import catering.persistence.PersistenceManager;
import catering.utils.DateUtils;
import catering.utils.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class StaffMemberCreationTest {
    private static final Logger LOGGER = LogManager.getLogger(StaffMemberCreationTest.class);

    private static CatERing app;
    private static StaffMember organizer;
    private static StaffMember notOrganizer;
    private static StaffMember owner;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();

        LOGGER.info("Starting test: StaffMemberCreationTest");
    }

    @BeforeEach
    void setup() {
        organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        assertNotNull(organizer, "'organizer' user should be loaded");
        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should have organizer role");

        notOrganizer = StaffMemberDAO.loadByEmail("luca.verdi@example.com");
        assertNotNull(notOrganizer, "'notOrganizer' user should be loaded");
        assertFalse(notOrganizer.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should not have organizer role");

        owner = StaffMemberDAO.loadByEmail("chiara.bruni@example.com");
        assertNotNull(owner, "'owner' user should be loaded");
        assertTrue(owner.hasRole(StaffMember.Role.ORGANIZZATORE), "Staff Member should have organizer role");
        assertTrue(owner.hasRole(StaffMember.Role.PROPRIETARIO), "Staff Member should have owner role");
    }

    // Adding new occasional staff member profile by Organizer
    @Test
    void createOccasionalStaffMemberProfileByOrganizer() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(organizer.getEmail());

        try {
            String email = "a@a.com";
            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                email,
                "Paolo",
                "Nesi",
                DateUtils.safeValueOf("1985-07-12"),
                "Via Savona 10, Torino",
                "+39 338 1882136",
                700,
                StaffMember.EmploymentType.OCCASIONALE
            );

            assertNotNull(newMember);
            StaffMember loaded = StaffMemberDAO.loadByEmail(email);
            assertNotNull(loaded, "New staff member should be persisted");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }


    // Adding new permanent staff member profile by Organizer (should fail)
    @Test
    void createPermanentStaffMemberProfileByOrganizer() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(organizer.getEmail());

        assertThrows(UseCaseLogicException.class, () -> {
            app.getStaffMemberManager().addNewStaffMember(
                "b@b.com",
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

    // Updating employment type from Occasional to Permanent by Organizer (should fail)
    @Test
    void updateToPermanentStaffMemberProfileByOrganizer() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(organizer.getEmail());

        assertThrows(UseCaseLogicException.class, () -> {
            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                "c@c.com",
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

    // Attempt to add Occasional staff member by NotOrganizer (should fail)
    @Test
    void createOccasionalStaffMemberProfileByNotOrganizer() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(notOrganizer.getEmail());

        assertThrows(UseCaseLogicException.class, () -> {
            app.getStaffMemberManager().addNewStaffMember(
                "d@d.com",
                "Mario",
                "Bianchi",
                DateUtils.safeValueOf("1990-02-01"),
                "Via Milano 5, Torino",
                "+39 345 6789012",
                600,
                StaffMember.EmploymentType.OCCASIONALE
            );
        });
    }

    // Attempt to add Permanent staff member by NotOrganizer (should fail)
    @Test
    void createPermanentStaffMemberProfileByNotOrganizer() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(notOrganizer.getEmail());

        assertThrows(UseCaseLogicException.class, () -> {
            app.getStaffMemberManager().addNewStaffMember(
                "e@e.com",
                "Laura",
                "Rossi",
                DateUtils.safeValueOf("1988-03-15"),
                "Via Napoli 8, Torino",
                "+39 347 2345678",
                800,
                StaffMember.EmploymentType.PERMANENTE
            );
        });
    }

    // Attempt to change employment type by NotOrganizer (should fail)
    @Test
    void updateEmploymentTypeByNotOrganizer() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(owner.getEmail());

        StaffMember member = null;
        try {
            String email = "f@f.com";
            member = app.getStaffMemberManager().addNewStaffMember(
                email,
                "Marco",
                "Neri",
                DateUtils.safeValueOf("1992-06-21"),
                "Via Firenze 3, Torino",
                "+39 348 7654321",
                650,
                StaffMember.EmploymentType.OCCASIONALE
            );

            StaffMember loaded = StaffMemberDAO.loadByEmail(email);
            assertNotNull(loaded, "New staff member should be persisted");

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }

        app.getStaffMemberManager().fakeLogin(notOrganizer.getEmail());

        StaffMember finalMember = member;
        assertThrows(UseCaseLogicException.class, () -> {
            app.getStaffMemberManager().changeEmploymentType(finalMember, StaffMember.EmploymentType.PERMANENTE);
        });
    }


    // Adding occasional staff member by Owner
    @Test
    void createOccasionalStaffMemberProfileByOwner() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(owner.getEmail());

        try {
            String email = "g@g.com";
            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                email,
                "Valeria",
                "Galli",
                DateUtils.safeValueOf("1991-04-18"),
                "Via Roma 12, Torino",
                "+39 349 1112233",
                750,
                StaffMember.EmploymentType.OCCASIONALE
            );
            assertNotNull(newMember);
            assertNotNull(StaffMemberDAO.loadByEmail(email), "New staff member should be persisted");
        } catch (UseCaseLogicException e) {
            fail("Owner should be allowed to create occasional staff: " + e.getMessage());
        }
    }


    // Adding permanent staff member by Owner
    @Test
    void createPermanentStaffMemberProfileByOwner() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(owner.getEmail());

        try {
            String email = "h@h.com";
            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                email,
                "Andrea",
                "Mori",
                DateUtils.safeValueOf("1980-11-09"),
                "Via Dante 22, Torino",
                "+39 350 2223344",
                1000,
                StaffMember.EmploymentType.PERMANENTE
            );
            assertNotNull(newMember);
            assertNotNull(StaffMemberDAO.loadByEmail(email), "New staff member should be persisted");
        } catch (UseCaseLogicException e) {
            fail("Owner should be allowed to create permanent staff: " + e.getMessage());
        }
    }


    // Changing employment type by Owner
    @Test
    void updateEmploymentTypeByOwner() throws UseCaseLogicException {
        app.getStaffMemberManager().fakeLogin(owner.getEmail());

        try {
            String email = "i@i.com";
            StaffMember newMember = app.getStaffMemberManager().addNewStaffMember(
                email,
                "Giulia",
                "Vanni",
                DateUtils.safeValueOf("1993-10-25"),
                "Via Po 6, Torino",
                "+39 351 3344556",
                720,
                StaffMember.EmploymentType.OCCASIONALE
            );

            assertNotNull(StaffMemberDAO.loadByEmail(email), "New staff member should be persisted");

            app.getStaffMemberManager().changeEmploymentType(newMember, StaffMember.EmploymentType.PERMANENTE);
            assertEquals(StaffMember.EmploymentType.PERMANENTE, newMember.getEmploymentType());

        } catch (UseCaseLogicException e) {
            fail("Owner should be allowed to change employment type: " + e.getMessage());
        }
    }

}
