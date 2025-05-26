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
import java.util.Optional;
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

        // Check if professor evaluation already exists
        if (traineeship.hasProfessorEvaluation()) {
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

        // Check if professor evaluation already exists
        if (traineeship.hasProfessorEvaluation()) {
            redirectAttrs.addFlashAttribute("error", "This traineeship has already been evaluated by a professor");
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
        ProfessorEvaluation evaluation = new ProfessorEvaluation();
        evaluation.setTraineeship(traineeship);
        evaluation.setStudentMotivation(studentMotivation);
        evaluation.setStudentEffectiveness(studentEffectiveness);
        evaluation.setStudentEfficiency(studentEfficiency);
        evaluation.setCompanyFacilities(companyFacilities);
        evaluation.setCompanyGuidance(companyGuidance);
        evaluation.setComments(comments);
        evaluation.setEvaluationDate(LocalDateTime.now());

        traineeship.setProfessorEvaluation(evaluation);
        traineeshipRepo.save(traineeship);

        redirectAttrs.addFlashAttribute("success", "Evaluation submitted successfully");
        return "redirect:/professor/traineeships";
    }

    @PostMapping("/evaluate/{id}")
    public String submitEvaluation(@PathVariable Long id, @ModelAttribute TraineeshipDto dto) {
        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        if (traineeship == null) {
            return "redirect:/professor/traineeships";
        }

        ProfessorEvaluation evaluation = new ProfessorEvaluation();
        evaluation.setTraineeship(traineeship);
        evaluation.setStudentMotivation(dto.getProfessorStudentMotivation());
        evaluation.setStudentEffectiveness(dto.getProfessorStudentEffectiveness());
        evaluation.setStudentEfficiency(dto.getProfessorStudentEfficiency());
        evaluation.setCompanyFacilities(dto.getProfessorCompanyFacilities());
        evaluation.setCompanyGuidance(dto.getProfessorCompanyGuidance());
        evaluation.setComments(dto.getProfessorComments());
        evaluation.setEvaluationDate(LocalDateTime.now());

        traineeship.setProfessorEvaluation(evaluation);
        traineeshipRepo.save(traineeship);

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

        // Handle professor evaluation
        if (traineeship.hasProfessorEvaluation()) {
            dto.setHasProfessorEvaluation(true);
            ProfessorEvaluation evaluation = traineeship.getProfessorEvaluation();
            dto.setProfessorStudentMotivation(evaluation.getStudentMotivation());
            dto.setProfessorStudentEffectiveness(evaluation.getStudentEffectiveness());
            dto.setProfessorStudentEfficiency(evaluation.getStudentEfficiency());
            dto.setProfessorCompanyFacilities(evaluation.getCompanyFacilities());
            dto.setProfessorCompanyGuidance(evaluation.getCompanyGuidance());
            dto.setProfessorComments(evaluation.getComments());
            dto.setProfessorEvaluationDate(evaluation.getEvaluationDate());
        } else {
            dto.setHasProfessorEvaluation(false);
        }

        return dto;
    }
} 