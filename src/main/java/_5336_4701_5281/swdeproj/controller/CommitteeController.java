package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/committee")
public class CommitteeController {

    private final ApplicationRepository applicationRepo;
    private final TraineeshipRepository traineeshipRepo;
    private final UserRepository userRepo;
    private final EvaluationRepository evaluationRepo;
    private final LogbookEntryRepository logbookRepo;
    private final NotificationService notificationService;

    public CommitteeController(ApplicationRepository applicationRepo,
                             TraineeshipRepository traineeshipRepo,
                             UserRepository userRepo,
                             EvaluationRepository evaluationRepo,
                             LogbookEntryRepository logbookRepo,
                             NotificationService notificationService) {
        this.applicationRepo = applicationRepo;
        this.traineeshipRepo = traineeshipRepo;
        this.userRepo = userRepo;
        this.evaluationRepo = evaluationRepo;
        this.logbookRepo = logbookRepo;
        this.notificationService = notificationService;
    }

    @GetMapping("/applications")
    public String viewApplications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.hasRole(User.Role.ROLE_COMMITTEE)) {
            return "redirect:/home";
        }

        List<Application> pendingApplications = applicationRepo.findByStatus(Application.Status.PENDING);
        List<User> professors = userRepo.findByRolesContaining(User.Role.ROLE_PROFESSOR);

        model.addAttribute("applications", pendingApplications);
        model.addAttribute("professors", professors);
        return "committee/applications";
    }

    @PostMapping("/applications/{id}/approve")
    public String approveApplication(@PathVariable Long id,
                                   @RequestParam Long supervisorId,
                                   RedirectAttributes redirectAttrs) {
        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        User supervisor = userRepo.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        // Update application status
        application.setStatus(Application.Status.APPROVED);
        application.setSupervisor(supervisor);
        applicationRepo.save(application);

        // Update traineeship status
        Traineeship traineeship = application.getTraineeship();
        traineeship.setStatus(Traineeship.Status.FILLED);
        traineeshipRepo.save(traineeship);

        // Send notification
        notificationService.notifyApplicationStatus(application);

        redirectAttrs.addFlashAttribute("success", "Application approved and supervisor assigned successfully");
        return "redirect:/committee/applications";
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(Application.Status.REJECTED);
        applicationRepo.save(application);

        // Send notification
        notificationService.notifyApplicationStatus(application);

        redirectAttrs.addFlashAttribute("success", "Application rejected successfully");
        return "redirect:/committee/applications";
    }

    @GetMapping("/overview")
    public String viewOverview(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.hasRole(User.Role.ROLE_COMMITTEE)) {
            return "redirect:/home";
        }

        // Gather statistics
        Map<String, Object> stats = new HashMap<>();
        
        // Traineeship statistics
        stats.put("totalTraineeships", traineeshipRepo.count());
        stats.put("openTraineeships", traineeshipRepo.countByStatus(Traineeship.Status.OPEN));
        stats.put("filledTraineeships", traineeshipRepo.countByStatus(Traineeship.Status.FILLED));
        
        // Application statistics
        stats.put("pendingApplications", applicationRepo.countByStatus(Application.Status.PENDING));
        stats.put("approvedApplications", applicationRepo.countByStatus(Application.Status.APPROVED));
        stats.put("rejectedApplications", applicationRepo.countByStatus(Application.Status.REJECTED));
        
        // Evaluation statistics
        stats.put("companyEvaluations", evaluationRepo.countByEvaluatorType(Evaluation.EvaluatorType.COMPANY));
        stats.put("professorEvaluations", evaluationRepo.countByEvaluatorType(Evaluation.EvaluatorType.PROFESSOR));
        
        // Average ratings
        stats.put("avgMotivationRating", evaluationRepo.findAverageMotivationRating());
        stats.put("avgEffectivenessRating", evaluationRepo.findAverageEffectivenessRating());
        stats.put("avgEfficiencyRating", evaluationRepo.findAverageEfficiencyRating());
        
        // Recent activity
        stats.put("recentApplications", applicationRepo.findTop5ByOrderByApplicationDateDesc());
        stats.put("recentEvaluations", evaluationRepo.findTop5ByOrderByDateDesc());

        model.addAttribute("stats", stats);
        return "committee/overview";
    }
} 