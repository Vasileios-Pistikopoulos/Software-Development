package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.TraineeshipDto;
import _5336_4701_5281.swdeproj.dto.EvaluationDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeshipControllerTest {

    @InjectMocks
    private TraineeshipController controller;

    @Mock private TraineeshipRepository traineeshipRepo;
    @Mock private ApplicationRepository applicationRepo;
    @Mock private UserRepository userRepo;
    @Mock private TraineeProfileRepository traineeRepo;
    @Mock private CompanyRepository companyRepo;
    @Mock private UserService userService;
    @Mock private EvaluationRepository evaluationRepo;
    @Mock private TraineeshipService traineeshipService;
    @Mock private ProfessorProfileRepository professorRepo;
    @Mock private Model model;
    @Mock private RedirectAttributes redirectAttrs;
    @Mock private Authentication authentication;
    @Mock private CompanyController companyController;
    @Mock private EvaluationController evaluationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TraineeshipController(
                traineeshipRepo, applicationRepo, userRepo, traineeRepo, companyRepo,
                userService, evaluationRepo, traineeshipService, professorRepo
        );
    }

    @Test
    void listTraineeships() {
        when(userService.getCurrentUser(authentication)).thenReturn(new User());
        when(traineeshipRepo.findByStatus(any())).thenReturn(Collections.emptyList());
        String view = controller.listTraineeships(model, authentication);
        assertEquals("traineeship/list", view);
        verify(model).addAttribute(eq("traineeships"), any());
    }

    @Test
    void viewTraineeship() {
        Traineeship t = new Traineeship();
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(t));
        String view = controller.viewTraineeship(1L, model);
        assertEquals("traineeship/view", view);
        verify(model).addAttribute(eq("traineeship"), any());
    }

    @Test
    void showApplicationForm() {
        Traineeship t = new Traineeship();
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(t));
        String view = controller.showApplicationForm(model, authentication);
        assertEquals("traineeship/apply", view);
        verify(model).addAttribute(eq("traineeship"), any());
    }

    @Test
    void submitApplication() {
        when(userService.getCurrentUser(authentication)).thenReturn(new User());
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = controller.submitApplication("1", "coverLetter", "additionalInfo", "otherField", redirectAttrs, authentication);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void showCreateForm() {
        String view = controller.showCreateForm(model);
        assertEquals("traineeship/create", view);
        verify(model).addAttribute(eq("traineeship"), any());
    }

    @Test
    void createTraineeship() {
        when(userService.getCurrentUser(authentication)).thenReturn(new User());
        String view = controller.createTraineeship(new TraineeshipDto(), redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void showEditForm() {
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = controller.showEditForm(1L, model);
        assertEquals("traineeship/edit", view);
        verify(model).addAttribute(eq("traineeship"), any());
    }

    @Test
    void updateTraineeship() {
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = controller.updateTraineeship(1L, new TraineeshipDto(), redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void deleteTraineeship() {
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = controller.deleteTraineeship(1L, redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void viewMyApplications() {
        when(userService.getCurrentUser(authentication)).thenReturn(new User());
        when(applicationRepo.findByTraineeId(anyLong())).thenReturn(Collections.emptyList());
        String view = controller.viewMyApplications(model);
        assertEquals("traineeship/my-applications", view);
        verify(model).addAttribute(eq("applications"), any());
    }

    @Test
    void withdrawApplication() {
        when(applicationRepo.findById(anyLong())).thenReturn(Optional.of(new Application()));
        String view = controller.withdrawApplication(1L, redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void showEvaluationForm() {
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = evaluationController.showCreateForm(1L, model);
        assertEquals("evaluation/create", view);
        verify(model).addAttribute(eq("evaluation"), any());
    }

    @Test
    void submitEvaluation() {
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = evaluationController.createEvaluation(1L, new EvaluationDto(), redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void assignSupervisorForm() {
        when(traineeshipService.getTraineeshipById(anyLong())).thenReturn(new Traineeship());
        when(traineeshipService.findSuitableProfessors(any())).thenReturn(Collections.emptyList());
        String view = controller.assignSupervisorForm(1L, model);
        assertEquals("traineeship/assign-supervisor", view);
        verify(model).addAttribute(eq("traineeship"), any());
        verify(model).addAttribute(eq("professors"), any());
    }

    @Test
    void assignSupervisor() {
        doNothing().when(traineeshipService).assignSupervisor(anyLong(), anyLong());
        String view = controller.assignSupervisor(1L, 1L, redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }

    @Test
    void completeTraineeship() {
        when(traineeshipRepo.findById(anyLong())).thenReturn(Optional.of(new Traineeship()));
        String view = controller.completeTraineeship(1L, "PASS", "comments", authentication, redirectAttrs);
        assertTrue(view.startsWith("redirect:"));
    }
}