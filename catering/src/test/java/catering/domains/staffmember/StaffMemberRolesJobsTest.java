package catering.domains.staffmember;

import catering.domains.staffmember.domain.StaffMember;
import catering.domains.staffmember.domain.StaffMemberManager;
import catering.domains.staffmember.infrastructure.StaffMemberDAO;
import catering.exceptions.UseCaseLogicException;
import catering.persistence.PersistenceManager;
import catering.utils.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class StaffMemberRolesJobsTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffMemberCreationTest.class);

    private static StaffMemberManager manager;
    private static StaffMember target;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        manager = new StaffMemberManager();

        LOGGER.info("Starting test: StaffMemberRolesJobsTest");
    }

    @BeforeEach
    void setup() throws UseCaseLogicException {
        manager.fakeLogin("giovanni.ricci@example.com");
        StaffMember organizer = StaffMemberDAO.loadByEmail("giovanni.ricci@example.com");
        target = StaffMemberDAO.loadByEmail("marco.rossi@example.com");
        assertNotNull(organizer);
        assertTrue(organizer.hasRole(StaffMember.Role.ORGANIZZATORE));
    }

    @Test
    void testHasRole() throws UseCaseLogicException {
        boolean hasRole = manager.hasRole(target, StaffMember.Role.SERVIZIO);
        assertEquals(target.hasRole(StaffMember.Role.SERVIZIO), hasRole);
    }

    @Test
    void testAddRoleWithJobs() throws UseCaseLogicException {
        Set<String> jobs = new HashSet<>();
        jobs.add("Buffet");
        jobs.add("Sala");

        boolean added = manager.addRole(target, StaffMember.Role.SERVIZIO, jobs);
        assertTrue(added, "Role should be added with jobs");
        assertTrue(target.hasRole(StaffMember.Role.SERVIZIO));
        assertTrue(target.getRoles().get(StaffMember.Role.SERVIZIO).contains("Buffet"));
    }

    @Test
    void testRemoveRole() throws UseCaseLogicException {
        Set<String> jobs = Set.of("Sala");
        manager.addRole(target, StaffMember.Role.SERVIZIO, jobs);

        boolean removed = manager.removeRole(target, StaffMember.Role.SERVIZIO);
        assertTrue(removed, "Role should be removed");
        assertFalse(target.hasRole(StaffMember.Role.SERVIZIO));
    }

    @Test
    void testRemoveJobsFromRole() throws UseCaseLogicException {
        Set<String> jobs = new HashSet<>();
        jobs.add("Buffet");
        jobs.add("Sala");
        manager.addRole(target, StaffMember.Role.SERVIZIO, jobs);

        Set<String> jobsToRemove = Set.of("Sala");
        boolean removed = manager.removeJobs(target, StaffMember.Role.SERVIZIO, jobsToRemove);

        assertTrue(removed, "Job should be removed from role");
        Set<String> remainingJobs = target.getRoles().get(StaffMember.Role.SERVIZIO);
        assertFalse(remainingJobs.contains("Sala"));
        assertTrue(remainingJobs.contains("Buffet"));
    }

    @Test
    void testAddJobsToExistingRole() throws UseCaseLogicException {
        // First, assign initial jobs to the role
        Set<String> initialJobs = Set.of("Buffet");
        boolean added = manager.addRole(target, StaffMember.Role.SERVIZIO, initialJobs);
        assertTrue(added || target.hasRole(StaffMember.Role.SERVIZIO));

        // Now add new jobs to the same role
        Set<String> newJobs = Set.of("Sala", "Cucina");
        boolean jobsAdded = manager.addRole(target, StaffMember.Role.SERVIZIO, newJobs); // Expected to merge

        assertTrue(jobsAdded, "New jobs should be added to existing role");
        Set<String> allJobs = target.getRoles().get(StaffMember.Role.SERVIZIO);
        assertTrue(allJobs.contains("Buffet"));
        assertTrue(allJobs.contains("Sala"));
        assertTrue(allJobs.contains("Cucina"));
    }
}
