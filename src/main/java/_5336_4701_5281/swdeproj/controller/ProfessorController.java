package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.TraineeshipDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/professor")
public class ProfessorController {

    private final TraineeshipRepository traineeshipRepo;
    private final TraineeshipEvaluationRepository evaluationRepo;
    private final UserService userService;
    private final ProfessorProfileRepository professorRepo;
    private final EvaluationRepository evaluationRepository;

    @Autowired
    public ProfessorController(TraineeshipRepository traineeshipRepo,
                             TraineeshipEvaluationRepository evaluationRepo,
                             UserService userService,
                             ProfessorProfileRepository professorRepo,
                             EvaluationRepository evaluationRepository) {
        this.traineeshipRepo = traineeshipRepo;
        this.evaluationRepo = evaluationRepo;
        this.userService = userService;
        this.professorRepo = professorRepo;
        this.evaluationRepository = evaluationRepository;
    }

    @GetMapping("/traineeships")
    public String listSupervisedTraineeships(Model model, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_PROFESSOR) {
            return "redirect:/";
        }

        ProfessorProfile professor = professorRepo.findByUserId(user.getId());
        if (professor == null) {
            return "redirect:/";
        }

        List<Traineeship> traineeships = traineeshipRepo.findBySupervisorId(professor.getId());
        
        List<TraineeshipDto> dtos = traineeships.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        model.addAttribute("traineeships", dtos);
        return "professor/supervised-traineeships";
    }

    @GetMapping("/traineeships/{id}/evaluate")
    public String showEvaluationForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_PROFESSOR) {
            return "redirect:/";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));

        // Check if the professor is the supervisor
        if (!traineeship.getSupervisor().getId().equals(user.getProfessorProfile().getId())) {
            return "redirect:/professor/traineeships";
        }

        // Check if evaluation already exists
        if (traineeship.hasEvaluation()) {
            return "redirect:/professor/traineeships";
        }

        model.addAttribute("traineeship", convertToDto(traineeship));
        return "professor/evaluate-traineeship";
    }

    @PostMapping("/traineeships/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
                                 @RequestParam Integer studentMotivation,
                                 @RequestParam Integer studentEffectiveness,
                                 @RequestParam Integer studentEfficiency,
                                 @RequestParam Integer companyFacilities,
                                 @RequestParam Integer companyGuidance,
                                 @RequestParam(required = false) String comments,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttrs) {
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_PROFESSOR) {
            return "redirect:/";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));

        // Check if the professor is the supervisor
        if (!traineeship.getSupervisor().getId().equals(user.getProfessorProfile().getId())) {
            redirectAttrs.addFlashAttribute("error", "You are not authorized to evaluate this traineeship");
            return "redirect:/professor/traineeships";
        }

        // Check if evaluation already exists
        if (traineeship.hasEvaluation()) {
            redirectAttrs.addFlashAttribute("error", "This traineeship has already been evaluated");
            return "redirect:/professor/traineeships";
        }

        // Validate ratings
        if (studentMotivation < 1 || studentMotivation > 5 ||
            studentEffectiveness < 1 || studentEffectiveness > 5 ||
            studentEfficiency < 1 || studentEfficiency > 5 ||
            companyFacilities < 1 || companyFacilities > 5 ||
            companyGuidance < 1 || companyGuidance > 5) {
            redirectAttrs.addFlashAttribute("error", "All ratings must be between 1 and 5");
            return "redirect:/professor/traineeships/" + id + "/evaluate";
        }

        // Create and save evaluation
        TraineeshipEvaluation evaluation = new TraineeshipEvaluation();
        evaluation.setTraineeship(traineeship);
        evaluation.setStudentMotivation(studentMotivation);
        evaluation.setStudentEffectiveness(studentEffectiveness);
        evaluation.setStudentEfficiency(studentEfficiency);
        evaluation.setCompanyFacilities(companyFacilities);
        evaluation.setCompanyGuidance(companyGuidance);
        evaluation.setComments(comments);
        evaluation.setEvaluationDate(LocalDateTime.now());

        traineeship.setEvaluation(evaluation);
        traineeshipRepo.save(traineeship);

        redirectAttrs.addFlashAttribute("success", "Evaluation submitted successfully");
        return "redirect:/professor/traineeships";
    }

    private TraineeshipDto convertToDto(Traineeship traineeship) {
        TraineeshipDto dto = new TraineeshipDto();
        dto.setId(traineeship.getId());
        dto.setTitle(traineeship.getTitle());
        dto.setDescription(traineeship.getDescription());
        dto.setStartDate(traineeship.getStartDate());
        dto.setEndDate(traineeship.getEndDate());
        dto.setSkillsRequired(traineeship.getRequiredSkills().stream().collect(Collectors.toList()));
        dto.setTopics(traineeship.getTopics().stream().collect(Collectors.toList()));
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

        // Check if professor evaluation exists
        boolean hasProfessorEvaluation = evaluationRepository
            .findByTraineeshipIdAndEvaluatorType(traineeship.getId(), Evaluation.EvaluatorType.PROFESSOR)
            .isPresent();
        dto.setHasEvaluation(hasProfessorEvaluation);

        // Add evaluation information if it exists
        if (hasProfessorEvaluation) {
            evaluationRepository.findByTraineeshipIdAndEvaluatorType(traineeship.getId(), Evaluation.EvaluatorType.PROFESSOR)
                .ifPresent(evaluation -> {
                    dto.setStudentMotivation(evaluation.getMotivationRating());
                    dto.setStudentEffectiveness(evaluation.getEffectivenessRating());
                    dto.setStudentEfficiency(evaluation.getEfficiencyRating());
                    dto.setCompanyFacilities(evaluation.getFacilitiesRating());
                    dto.setCompanyGuidance(evaluation.getGuidanceRating());
                    dto.setEvaluationComments(evaluation.getComments());
                    dto.setEvaluationDate(evaluation.getDate().atStartOfDay());
                });
        }

        return dto;
    }
} 