package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.TraineeshipDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/company")
public class CompanyController {

    private final TraineeshipRepository traineeshipRepo;
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final EvaluationRepository evaluationRepo;
    private final UserService userService;

    public CompanyController(TraineeshipRepository traineeshipRepo,
                            CompanyRepository companyRepo,
                            UserRepository userRepo,
                            EvaluationRepository evaluationRepo,
                            UserService userService) {
        this.traineeshipRepo = traineeshipRepo;
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.evaluationRepo = evaluationRepo;
        this.userService = userService;
    }

    @GetMapping("/assigned-traineeships")
    public String viewAssignedTraineeships(Model model, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        Company company = companyRepo.findByUserId(user.getId());
        
        List<Traineeship> assignedTraineeships = traineeshipRepo.findByCompanyIdAndAssignedTraineeIsNotNull(company.getId());
        List<TraineeshipDto> dtos = assignedTraineeships.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        model.addAttribute("assignedTraineeships", dtos);
        return "company/assigned-traineeships";
    }

    @PostMapping("/evaluations/submit")
    public String submitEvaluation(@RequestParam Long traineeshipId,
                                 @RequestParam Integer motivation,
                                 @RequestParam Integer effectiveness,
                                 @RequestParam Integer efficiency,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttrs) {
        User user = userService.getCurrentUser(authentication);
        Company company = companyRepo.findByUserId(user.getId());
        
        Traineeship traineeship = traineeshipRepo.findById(traineeshipId)
            .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        // Verify that the company owns this traineeship
        if (!traineeship.getCompany().getId().equals(company.getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only evaluate your own traineeships");
            return "redirect:/company/assigned-traineeships";
        }
        
        // Check if evaluation already exists
        if (evaluationRepo.findByTraineeshipIdAndEvaluatorType(traineeshipId, Evaluation.EvaluatorType.COMPANY).isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Evaluation already submitted for this traineeship");
            return "redirect:/company/assigned-traineeships";
        }
        
        // Create and save evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setTraineeship(traineeship);
        evaluation.setEvaluatorType(Evaluation.EvaluatorType.COMPANY);
        evaluation.setMotivationRating(motivation);
        evaluation.setEffectivenessRating(effectiveness);
        evaluation.setEfficiencyRating(efficiency);
        evaluation.setFacilitiesRating(5); // Default value for company evaluations
        evaluation.setGuidanceRating(5); // Default value for company evaluations
        evaluation.setDate(LocalDate.now());
        evaluation.setEvaluator(user);
        
        evaluationRepo.save(evaluation);
        
        redirectAttrs.addFlashAttribute("success", "Evaluation submitted successfully");
        return "redirect:/company/assigned-traineeships";
    }

    private TraineeshipDto convertToDto(Traineeship traineeship) {
        TraineeshipDto dto = new TraineeshipDto();
        dto.setId(traineeship.getId());
        dto.setTitle(traineeship.getTitle());
        dto.setDescription(traineeship.getDescription());
        dto.setStartDate(traineeship.getStartDate());
        dto.setEndDate(traineeship.getEndDate());
        dto.setLocation(traineeship.getLocation());
        dto.setCompanyId(traineeship.getCompany().getId());
        dto.setCompanyName(traineeship.getCompany().getName());
        dto.setStatus(traineeship.getStatus());
        
        if (traineeship.getAssignedTrainee() != null) {
            dto.setAssignedTraineeId(traineeship.getAssignedTrainee().getId());
            dto.setAssignedTraineeName(traineeship.getAssignedTrainee().getFullName());
            dto.setAssigned(true);
        }
        
        if (traineeship.getSupervisor() != null) {
            dto.setSupervisorId(traineeship.getSupervisor().getId());
            dto.setSupervisorName(traineeship.getSupervisor().getFullName());
        }

        // Check if company evaluation exists
        dto.setHasEvaluation(evaluationRepo.findByTraineeshipIdAndEvaluatorType(
            traineeship.getId(), Evaluation.EvaluatorType.COMPANY).isPresent());

        return dto;
    }
} 