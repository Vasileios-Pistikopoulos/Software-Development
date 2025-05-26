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
import java.time.LocalDateTime;
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

    @PostMapping("/traineeships/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
                                 @RequestParam Integer studentMotivation,
                                 @RequestParam Integer studentEffectiveness,
                                 @RequestParam Integer studentEfficiency,
                                 @RequestParam(required = false) String comments,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttrs) {
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            return "redirect:/";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));

        // Check if the company is the owner of the traineeship
        if (!traineeship.getCompany().getId().equals(user.getCompanyProfile().getId())) {
            redirectAttrs.addFlashAttribute("error", "You are not authorized to evaluate this traineeship");
            return "redirect:/company/traineeships";
        }

        // Check if company evaluation already exists
        if (traineeship.hasCompanyEvaluation()) {
            redirectAttrs.addFlashAttribute("error", "This traineeship has already been evaluated by the company");
            return "redirect:/company/traineeships";
        }

        // Validate ratings
        if (studentMotivation < 1 || studentMotivation > 5 ||
            studentEffectiveness < 1 || studentEffectiveness > 5 ||
            studentEfficiency < 1 || studentEfficiency > 5) {
            redirectAttrs.addFlashAttribute("error", "All ratings must be between 1 and 5");
            return "redirect:/company/traineeships/" + id + "/evaluate";
        }

        // Create and save evaluation
        CompanyEvaluation evaluation = new CompanyEvaluation();
        evaluation.setTraineeship(traineeship);
        evaluation.setStudentMotivation(studentMotivation);
        evaluation.setStudentEffectiveness(studentEffectiveness);
        evaluation.setStudentEfficiency(studentEfficiency);
        evaluation.setComments(comments);
        evaluation.setEvaluationDate(LocalDateTime.now());

        traineeship.setCompanyEvaluation(evaluation);
        traineeshipRepo.save(traineeship);

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

        // Handle company evaluation
        if (traineeship.hasCompanyEvaluation()) {
            dto.setHasCompanyEvaluation(true);
            CompanyEvaluation evaluation = traineeship.getCompanyEvaluation();
            dto.setCompanyStudentMotivation(evaluation.getStudentMotivation());
            dto.setCompanyStudentEffectiveness(evaluation.getStudentEffectiveness());
            dto.setCompanyStudentEfficiency(evaluation.getStudentEfficiency());
            dto.setCompanyComments(evaluation.getComments());
            dto.setCompanyEvaluationDate(evaluation.getEvaluationDate());
        } else {
            dto.setHasCompanyEvaluation(false);
        }

        return dto;
    }
} 