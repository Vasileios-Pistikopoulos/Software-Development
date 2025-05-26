package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogbookControllerTest {

    @Mock
    private LogbookEntryRepository logbookRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private TraineeProfileRepository traineeRepo;
    @Mock
    private TraineeshipRepository traineeshipRepo;
    @Mock
    private Model model;
    @Mock
    private RedirectAttributes redirectAttrs;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private User user;

    @InjectMocks
    private LogbookController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LogbookController(logbookRepo, userRepo, traineeRepo, traineeshipRepo);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void viewLogbookReturnsLogbookView() {
        User user = new User();
        user.setUsername("trainee");
        user.setRole(User.Role.ROLE_TRAINEE);
        TraineeProfile trainee = new TraineeProfile();
        trainee.setUser(user);
        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);

        String view = controller.viewLogbook(model, redirectAttrs);
        assertEquals("logbook/list", view);
        verify(model).addAttribute(eq("entries"), any());
    }

    @Test
    void showCreateFormReturnsCreateView() {
        User user = new User();
        user.setUsername("trainee");
        user.setRole(User.Role.ROLE_TRAINEE);
        TraineeProfile trainee = new TraineeProfile();
        trainee.setUser(user);
        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);

        String view = controller.showCreateForm(model, redirectAttrs);
        assertEquals("logbook/create", view);
        verify(model).addAttribute(eq("logbookEntry"), any(LogbookEntry.class));
    }

    @Test
    void createEntryRedirectsWhenNoActiveTraineeship() {
        User user = new User();
        user.setUsername("trainee");
        user.setRole(User.Role.ROLE_TRAINEE);
        setId(user, 1L);
        TraineeProfile trainee = new TraineeProfile();
        setId(trainee, 1L);
        trainee.setUser(user);
        LogbookEntry entry = new LogbookEntry();
        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);
        when(traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(getId(trainee))).thenReturn(null);

        String view = controller.createEntry(entry, redirectAttrs);
        assertEquals("redirect:/logbook", view);
        verify(redirectAttrs).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    void showEditFormRedirectsIfNotOwner() {
        LogbookEntry entry = new LogbookEntry();
        User user = new User();
        setId(user, 2L);
        user.setUsername("trainee");
        TraineeProfile trainee = new TraineeProfile();
        setId(trainee, 3L);
        trainee.setUser(user);
        User otherUser = new User();
        setId(otherUser, 4L);
        entry.setTrainee(otherUser);

        when(logbookRepo.findById(1L)).thenReturn(Optional.of(entry));
        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);

        String view = controller.showEditForm(1L, model);
        assertEquals("redirect:/logbook", view);
    }

    @Test
    void updateEntryUpdatesAndRedirects() {
        LogbookEntry entry = new LogbookEntry();
        User user = new User();
        setId(user, 2L);
        TraineeProfile trainee = new TraineeProfile();
        setId(trainee, 3L);
        trainee.setUser(user);
        entry.setTrainee(user);

        LogbookEntry updated = new LogbookEntry();
        updated.setDate(LocalDate.now());
        updated.setTaskDescription("desc");
        updated.setHoursSpent(2.0);
        updated.setLearningOutcomes("outcomes");

        when(logbookRepo.findById(1L)).thenReturn(Optional.of(entry));
        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);

        String view = controller.updateEntry(1L, updated, redirectAttrs);
        assertEquals("redirect:/logbook", view);
        verify(logbookRepo).save(entry);
        verify(redirectAttrs).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    void deleteEntryDeletesAndRedirects() {
        LogbookEntry entry = new LogbookEntry();
        User user = new User();
        setId(user, 2L);
        TraineeProfile trainee = new TraineeProfile();
        setId(trainee, 3L);
        trainee.setUser(user);
        entry.setTrainee(user);

        when(logbookRepo.findById(1L)).thenReturn(Optional.of(entry));
        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);

        String view = controller.deleteEntry(1L, redirectAttrs);
        assertEquals("redirect:/logbook", view);
        verify(logbookRepo).delete(entry);
        verify(redirectAttrs).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    void approveEntryApprovesIfProfessorAndSupervisor() {
        LogbookEntry entry = new LogbookEntry();
        Traineeship traineeship = new Traineeship();
        User professor = new User();
        setId(professor, 10L);
        professor.setRole(User.Role.ROLE_PROFESSOR);
        ProfessorProfile professorProfile = new ProfessorProfile();
        professorProfile.setUser(professor);
        traineeship.setSupervisor(professorProfile);
        entry.setTraineeship(traineeship);

        when(logbookRepo.findById(1L)).thenReturn(Optional.of(entry));
        when(authentication.getName()).thenReturn("prof");
        when(userRepo.findByUsername("prof")).thenReturn(Optional.of(professor));

        String view = controller.approveEntry(1L, redirectAttrs);
        assertEquals("redirect:/logbook", view);
        verify(logbookRepo).save(entry);
        verify(redirectAttrs).addFlashAttribute(eq("success"), anyString());
        assertEquals(LogbookEntry.Status.APPROVED, entry.getStatus());
    }

    @Test
    void rejectEntryRejectsWithReason() {
        LogbookEntry entry = new LogbookEntry();
        Traineeship traineeship = new Traineeship();
        User professor = new User();
        setId(professor, 10L);
        professor.setRole(User.Role.ROLE_PROFESSOR);
        ProfessorProfile professorProfile = new ProfessorProfile();
        professorProfile.setUser(professor);
        traineeship.setSupervisor(professorProfile);
        entry.setTraineeship(traineeship);

        when(logbookRepo.findById(1L)).thenReturn(Optional.of(entry));
        when(authentication.getName()).thenReturn("prof");
        when(userRepo.findByUsername("prof")).thenReturn(Optional.of(professor));

        String view = controller.rejectEntry(1L, "Not enough detail", redirectAttrs);
        assertEquals("redirect:/logbook", view);
        verify(logbookRepo).save(entry);
        verify(redirectAttrs).addFlashAttribute(eq("success"), anyString());
        assertEquals(LogbookEntry.Status.REJECTED, entry.getStatus());
        assertEquals("Not enough detail", entry.getComments());
    }

    @Test
    void viewStatisticsReturnsStatisticsView() {
        User user = new User();
        setId(user, 2L);
        user.setRole(User.Role.ROLE_TRAINEE);
        TraineeProfile trainee = new TraineeProfile();
        setId(trainee, 3L);
        trainee.setUser(user);
        Traineeship traineeship = new Traineeship();
        setId(traineeship, 4L);

        when(authentication.getName()).thenReturn("trainee");
        when(userRepo.findByUsername("trainee")).thenReturn(Optional.of(user));
        when(traineeRepo.findByUserId(getId(user))).thenReturn(trainee);
        when(traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(getId(trainee))).thenReturn(traineeship);
        when(logbookRepo.sumHoursByTraineeAndTraineeship(getId(trainee), getId(traineeship))).thenReturn(10.0);
        when(logbookRepo.countApprovedEntriesByTraineeAndTraineeship(getId(trainee), getId(traineeship), LogbookEntry.Status.APPROVED)).thenReturn(2L);

        String view = controller.viewStatistics(model);
        assertEquals("logbook/statistics", view);
        verify(model).addAttribute("totalHours", 10.0);
        verify(model).addAttribute("approvedEntries", 2L);
        verify(model).addAttribute("traineeship", traineeship);
    }

    // Helper method to set private ID fields via reflection
    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id on " + entity.getClass().getSimpleName(), e);
        }
    }

    private static Long getId(Object entity) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get id from " + entity.getClass().getSimpleName(), e);
        }
    }
}
