package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.EvaluationDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/evaluations")
public class EvaluationController {

    private final EvaluationRepository evaluationRepo;
    private final UserRepository userRepo;
    private final TraineeshipRepository traineeshipRepo;
    private final CompanyRepository companyRepo;
    private final ProfessorProfileRepository professorRepo;

    public EvaluationController(EvaluationRepository evaluationRepo,
                               UserRepository userRepo,
                               TraineeshipRepository traineeshipRepo,
                               CompanyRepository companyRepo,
                               ProfessorProfileRepository professorRepo) {
        this.evaluationRepo = evaluationRepo;
        this.userRepo = userRepo;
        this.traineeshipRepo = traineeshipRepo;
        this.companyRepo = companyRepo;
        this.professorRepo = professorRepo;
    }

    @GetMapping
    public String listEvaluations(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Traineeship> traineeships;
        Evaluation.EvaluatorType evaluatorType;

        switch (user.getRole()) {
            case ROLE_COMPANY -> {
                Company company = companyRepo.findByUserId(user.getId());
                traineeships = traineeshipRepo.findByCompanyIdAndAssignedTraineeIsNotNull(company.getId());
                evaluatorType = Evaluation.EvaluatorType.COMPANY;
            }
            case ROLE_PROFESSOR -> {
                ProfessorProfile professor = professorRepo.findByUserId(user.getId());
                traineeships = traineeshipRepo.findBySupervisorId(professor.getId());
                evaluatorType = Evaluation.EvaluatorType.PROFESSOR;
            }
            default -> {
                return "redirect:/home";
            }
        }

        List<EvaluationDto> evaluations = traineeships.stream()
                .map(traineeship -> {
                    EvaluationDto dto = new EvaluationDto();
                    dto.setTraineeshipId(traineeship.getId());
                    dto.setTraineeName(traineeship.getAssignedTrainee().getFullName());
                    dto.setCompanyName(traineeship.getCompany().getName());
                    
                    // Check if evaluation exists
                    evaluationRepo.findByTraineeshipIdAndEvaluatorType(traineeship.getId(), evaluatorType)
                            .ifPresent(evaluation -> {
                                dto.setId(evaluation.getId());
                                dto.setMotivation(evaluation.getMotivationRating());
                                dto.setEffectiveness(evaluation.getEffectivenessRating());
                                dto.setEfficiency(evaluation.getEfficiencyRating());
                                dto.setFacilities(evaluation.getFacilitiesRating());
                                dto.setGuidance(evaluation.getGuidanceRating());
                            });
                    
                    return dto;
                })
                .collect(Collectors.toList());

        model.addAttribute("evaluations", evaluations);
        model.addAttribute("evaluatorType", evaluatorType);
        return "evaluation/list";
    }

    @GetMapping("/create/{traineeshipId}")
    public String showCreateForm(@PathVariable Long traineeshipId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Traineeship traineeship = traineeshipRepo.findById(traineeshipId)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));

        // Verify authorization
        boolean isAuthorized = false;
        Evaluation.EvaluatorType evaluatorType = null;

        switch (user.getRole()) {
            case ROLE_COMPANY -> {
                Company company = companyRepo.findByUserId(user.getId());
                if (traineeship.getCompany().getId().equals(company.getId())) {
                    isAuthorized = true;
                    evaluatorType = Evaluation.EvaluatorType.COMPANY;
                }
            }
            case ROLE_PROFESSOR -> {
                ProfessorProfile professor = professorRepo.findByUserId(user.getId());
                if (traineeship.getSupervisor().getId().equals(professor.getId())) {
                    isAuthorized = true;
                    evaluatorType = Evaluation.EvaluatorType.PROFESSOR;
                }
            }
        }

        if (!isAuthorized) {
            return "redirect:/evaluations";
        }

        // Check if evaluation already exists
        if (evaluationRepo.findByTraineeshipIdAndEvaluatorType(traineeshipId, evaluatorType).isPresent()) {
            return "redirect:/evaluations";
        }

        EvaluationDto evaluationDto = new EvaluationDto();
        evaluationDto.setTraineeshipId(traineeshipId);
        evaluationDto.setTraineeName(traineeship.getAssignedTrainee().getFullName());
        evaluationDto.setCompanyName(traineeship.getCompany().getName());
        evaluationDto.setEvaluatorType(evaluatorType);

        model.addAttribute("evaluation", evaluationDto);
        return "evaluation/create";
    }

    @PostMapping("/create/{traineeshipId}")
    public String createEvaluation(@PathVariable Long traineeshipId,
                                 @ModelAttribute EvaluationDto evaluationDto,
                                 RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Traineeship traineeship = traineeshipRepo.findById(traineeshipId)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));

        // Verify authorization and set evaluator type
        Evaluation.EvaluatorType evaluatorType = null;
        switch (user.getRole()) {
            case ROLE_COMPANY -> {
                Company company = companyRepo.findByUserId(user.getId());
                if (traineeship.getCompany().getId().equals(company.getId())) {
                    evaluatorType = Evaluation.EvaluatorType.COMPANY;
                }
            }
            case ROLE_PROFESSOR -> {
                ProfessorProfile professor = professorRepo.findByUserId(user.getId());
                if (traineeship.getSupervisor().getId().equals(professor.getId())) {
                    evaluatorType = Evaluation.EvaluatorType.PROFESSOR;
                }
            }
        }

        if (evaluatorType == null) {
            redirectAttrs.addFlashAttribute("error", "Not authorized to evaluate this traineeship");
            return "redirect:/evaluations";
        }

        // Create and save evaluation
        Evaluation evaluation = new Evaluation(
                traineeship,
                evaluatorType,
                evaluationDto.getMotivation(),
                evaluationDto.getEffectiveness(),
                evaluationDto.getEfficiency()
        );
        evaluation.setFacilitiesRating(evaluationDto.getFacilities());
        evaluation.setGuidanceRating(evaluationDto.getGuidance());

        evaluationRepo.save(evaluation);
        redirectAttrs.addFlashAttribute("success", "Evaluation submitted successfully");
        return "redirect:/evaluations";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Evaluation evaluation = evaluationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify authorization
        boolean isAuthorized = false;
        switch (user.getRole()) {
            case ROLE_COMPANY -> {
                Company company = companyRepo.findByUserId(user.getId());
                if (evaluation.getTraineeship().getCompany().getId().equals(company.getId())) {
                    isAuthorized = true;
                }
            }
            case ROLE_PROFESSOR -> {
                ProfessorProfile professor = professorRepo.findByUserId(user.getId());
                if (evaluation.getTraineeship().getSupervisor().getId().equals(professor.getId())) {
                    isAuthorized = true;
                }
            }
        }

        if (!isAuthorized) {
            return "redirect:/evaluations";
        }

        EvaluationDto dto = new EvaluationDto();
        dto.setId(evaluation.getId());
        dto.setTraineeshipId(evaluation.getTraineeship().getId());
        dto.setTraineeName(evaluation.getTraineeship().getAssignedTrainee().getFullName());
        dto.setCompanyName(evaluation.getTraineeship().getCompany().getName());
        dto.setEvaluatorType(evaluation.getEvaluatorType());
        dto.setMotivation(evaluation.getMotivationRating());
        dto.setEffectiveness(evaluation.getEffectivenessRating());
        dto.setEfficiency(evaluation.getEfficiencyRating());
        dto.setFacilities(evaluation.getFacilitiesRating());
        dto.setGuidance(evaluation.getGuidanceRating());

        model.addAttribute("evaluation", dto);
        return "evaluation/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateEvaluation(@PathVariable Long id,
                                 @ModelAttribute EvaluationDto evaluationDto,
                                 RedirectAttributes redirectAttrs) {
        Evaluation evaluation = evaluationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify authorization
        boolean isAuthorized = false;
        switch (user.getRole()) {
            case ROLE_COMPANY -> {
                Company company = companyRepo.findByUserId(user.getId());
                if (evaluation.getTraineeship().getCompany().getId().equals(company.getId())) {
                    isAuthorized = true;
                }
            }
            case ROLE_PROFESSOR -> {
                ProfessorProfile professor = professorRepo.findByUserId(user.getId());
                if (evaluation.getTraineeship().getSupervisor().getId().equals(professor.getId())) {
                    isAuthorized = true;
                }
            }
        }

        if (!isAuthorized) {
            redirectAttrs.addFlashAttribute("error", "Not authorized to edit this evaluation");
            return "redirect:/evaluations";
        }

        evaluation.setMotivationRating(evaluationDto.getMotivation());
        evaluation.setEffectivenessRating(evaluationDto.getEffectiveness());
        evaluation.setEfficiencyRating(evaluationDto.getEfficiency());
        evaluation.setFacilitiesRating(evaluationDto.getFacilities());
        evaluation.setGuidanceRating(evaluationDto.getGuidance());

        evaluationRepo.save(evaluation);
        redirectAttrs.addFlashAttribute("success", "Evaluation updated successfully");
        return "redirect:/evaluations";
    }
} 