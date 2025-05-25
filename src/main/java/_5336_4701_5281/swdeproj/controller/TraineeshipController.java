package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.TraineeshipDto;
import _5336_4701_5281.swdeproj.dto.ApplicationDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.service.UserService;
import _5336_4701_5281.swdeproj.service.TraineeshipService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/traineeships")
public class TraineeshipController {

    private final TraineeshipRepository traineeshipRepo;
    private final ApplicationRepository applicationRepo;
    private final UserRepository userRepo;
    private final TraineeProfileRepository traineeRepo;
    private final CompanyRepository companyRepo;
    private final UserService userService;
    private final EvaluationRepository evaluationRepo;
    private final TraineeshipService traineeshipService;
    private final ProfessorProfileRepository professorRepo;

    public TraineeshipController(TraineeshipRepository traineeshipRepo,
                                ApplicationRepository applicationRepo,
                                UserRepository userRepo,
                                TraineeProfileRepository traineeRepo,
                                CompanyRepository companyRepo,
                                UserService userService,
                                EvaluationRepository evaluationRepo,
                                TraineeshipService traineeshipService,
                                ProfessorProfileRepository professorRepo) {
        this.traineeshipRepo = traineeshipRepo;
        this.applicationRepo = applicationRepo;
        this.userRepo = userRepo;
        this.traineeRepo = traineeRepo;
        this.companyRepo = companyRepo;
        this.userService = userService;
        this.evaluationRepo = evaluationRepo;
        this.traineeshipService = traineeshipService;
        this.professorRepo = professorRepo;
    }

    @GetMapping
    public String listTraineeships(Model model, Authentication authentication) {
        List<Traineeship> traineeships;
        
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            User user = userService.getCurrentUser(authentication);
            
            if (user.getRole() == User.Role.ROLE_COMPANY) {
                // Companies see all their traineeships
                Company company = companyRepo.findByUserId(user.getId());
                traineeships = traineeshipRepo.findByCompanyId(company.getId());
            } else if (user.getRole() == User.Role.ROLE_COMMITTEE) {
                // Committee members see all filled traineeships
                traineeships = traineeshipRepo.findByStatus(Traineeship.Status.FILLED);
            } else if (user.getRole() == User.Role.ROLE_PROFESSOR) {
                // Professors see traineeships they supervise
                ProfessorProfile professor = professorRepo.findByUserId(user.getId());
                if (professor != null) {
                    traineeships = traineeshipRepo.findBySupervisorId(professor.getId());
                } else {
                    traineeships = List.of(); // Empty list if no professor profile found
                }
            } else {
                // Students and others only see available positions
                traineeships = traineeshipRepo.findByStatus(Traineeship.Status.OPEN);
            }
        } else {
            // Anonymous users only see available positions
            traineeships = traineeshipRepo.findByStatus(Traineeship.Status.OPEN);
        }
        
        List<TraineeshipDto> dtos = traineeships.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        model.addAttribute("traineeships", dtos);
        return "traineeship/list";
    }

    @GetMapping("/{id}")
    public String viewTraineeship(@PathVariable Long id, Model model) {
        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        TraineeshipDto dto = convertToDto(traineeship);
        model.addAttribute("traineeship", dto);

        // Check if current user is the owner
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            User user = userRepo.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() == User.Role.ROLE_COMPANY) {
                Company company = companyRepo.findByUserId(user.getId());
                model.addAttribute("isOwner", company.getId().equals(traineeship.getCompany().getId()));
            }
            
            if (user.getRole() == User.Role.ROLE_TRAINEE) {
                TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
                boolean hasApplied = applicationRepo.findByTraineeIdAndAssignedTraineeshipId(
                        trainee.getId(), id).isPresent();
                model.addAttribute("hasApplied", hasApplied);
            }
        }

        return "traineeship/view";
    }

    @GetMapping("/apply")
    public String showApplicationForm(Model model, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_TRAINEE) {
            return "redirect:/traineeships";
        }

        TraineeProfile traineeProfile = traineeRepo.findByUserId(user.getId());
        model.addAttribute("traineeProfile", traineeProfile);
        return "traineeship/apply";
    }

    @PostMapping("/apply")
    public String submitApplication(@RequestParam String skills,
                                  @RequestParam String interests,
                                  @RequestParam String preferredLocation,
                                  @RequestParam(required = false) String coverLetter,
                                  RedirectAttributes redirectAttrs,
                                  Authentication authentication) {
        try {
            if (authentication == null) {
                redirectAttrs.addFlashAttribute("error", "You must be logged in to submit an application");
                return "redirect:/login";
            }

            User user = userService.getCurrentUser(authentication);
            if (user == null) {
                redirectAttrs.addFlashAttribute("error", "User not found");
                return "redirect:/login";
            }

            if (user.getRole() != User.Role.ROLE_TRAINEE) {
                redirectAttrs.addFlashAttribute("error", "Only trainees can apply for traineeships");
                return "redirect:/traineeships";
            }

            TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
            if (trainee == null) {
                redirectAttrs.addFlashAttribute("error", "Trainee profile not found. Please complete your profile first.");
                return "redirect:/profile";
            }
            
            // Check if already has a pending application
            List<Application> pendingApplications = applicationRepo.findByTraineeIdAndStatus(trainee.getId(), Application.Status.PENDING);
            if (!pendingApplications.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "You already have a pending application");
                return "redirect:/traineeships/my-applications";
            }

            // Validate input
            if (skills == null || skills.trim().isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Skills cannot be empty");
                return "redirect:/traineeships/apply";
            }
            if (interests == null || interests.trim().isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Interests cannot be empty");
                return "redirect:/traineeships/apply";
            }
            if (preferredLocation == null || preferredLocation.trim().isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Preferred location cannot be empty");
                return "redirect:/traineeships/apply";
            }

            // Update trainee profile with new preferences
            trainee.setSkills(new HashSet<>(Arrays.asList(skills.split(","))));
            trainee.setInterests(new HashSet<>(Arrays.asList(interests.split(","))));
            trainee.setPreferredLocation(preferredLocation);
            traineeRepo.save(trainee);

            // Create new application
            Application application = new Application(trainee);
            application.setCoverLetter(coverLetter != null ? coverLetter : "");
            application.setStatus(Application.Status.PENDING);
            application.setApplicationDate(LocalDateTime.now());
            applicationRepo.save(application);

            redirectAttrs.addFlashAttribute("success", "Application submitted successfully");
            return "redirect:/traineeships/my-applications";
        } catch (Exception e) {
            e.printStackTrace(); // This will log the full stack trace
            String errorMessage = "An error occurred while submitting your application: " + 
                                (e.getMessage() != null ? e.getMessage() : "Unknown error");
            redirectAttrs.addFlashAttribute("error", errorMessage);
            return "redirect:/traineeships/apply";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            return "redirect:/traineeships";
        }

        model.addAttribute("traineeshipDto", new TraineeshipDto());
        return "traineeship/create";
    }

    @PostMapping("/create")
    public String createTraineeship(@ModelAttribute TraineeshipDto dto, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            redirectAttrs.addFlashAttribute("error", "Only companies can create traineeships");
            return "redirect:/traineeships";
        }
        
        Company company = companyRepo.findByUserId(user.getId());
        
        Traineeship traineeship = new Traineeship();
        traineeship.setTitle(dto.getTitle());
        traineeship.setDescription(dto.getDescription());
        traineeship.setStartDate(dto.getStartDate());
        traineeship.setEndDate(dto.getEndDate());
        traineeship.setRequiredSkills(new HashSet<>(dto.getSkillsRequired()));
        traineeship.setTopics(new HashSet<>(dto.getTopics()));
        traineeship.setLocation(dto.getLocation());
        traineeship.setCompany(company);
        traineeship.setStatus(Traineeship.Status.OPEN);

        traineeshipRepo.save(traineeship);
        redirectAttrs.addFlashAttribute("success", "Traineeship created successfully");
        return "redirect:/traineeships";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            return "redirect:/traineeships";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        // Check if the company owns this traineeship
        Company company = companyRepo.findByUserId(user.getId());
        if (!company.getId().equals(traineeship.getCompany().getId())) {
            return "redirect:/traineeships";
        }

        TraineeshipDto dto = convertToDto(traineeship);
        model.addAttribute("traineeship", dto);  // For displaying current values
        model.addAttribute("traineeshipDto", dto);  // For form binding
        return "traineeship/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateTraineeship(@PathVariable Long id, 
                                  @ModelAttribute TraineeshipDto dto,
                                  RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            redirectAttrs.addFlashAttribute("error", "Only companies can edit traineeships");
            return "redirect:/traineeships";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        // Check if the company owns this traineeship
        Company company = companyRepo.findByUserId(user.getId());
        if (!company.getId().equals(traineeship.getCompany().getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only edit your own traineeships");
            return "redirect:/traineeships";
        }

        // Update traineeship fields
        traineeship.setTitle(dto.getTitle());
        traineeship.setDescription(dto.getDescription());
        traineeship.setStartDate(dto.getStartDate());
        traineeship.setEndDate(dto.getEndDate());
        traineeship.setLocation(dto.getLocation());
        traineeship.setRequiredSkills(new HashSet<>(dto.getSkillsRequired()));
        traineeship.setTopics(new HashSet<>(dto.getTopics()));

        traineeshipRepo.save(traineeship);
        redirectAttrs.addFlashAttribute("success", "Traineeship updated successfully");
        return "redirect:/traineeships/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteTraineeship(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            redirectAttrs.addFlashAttribute("error", "Only companies can delete traineeships");
            return "redirect:/traineeships";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        // Check if the company owns this traineeship
        Company company = companyRepo.findByUserId(user.getId());
        if (!company.getId().equals(traineeship.getCompany().getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only delete your own traineeships");
            return "redirect:/traineeships";
        }

        // Check if the position is already assigned
        if (traineeship.getStatus() == Traineeship.Status.FILLED) {
            redirectAttrs.addFlashAttribute("error", "Cannot delete a traineeship that has been assigned");
            return "redirect:/traineeships/" + id;
        }

        traineeshipRepo.delete(traineeship);
        redirectAttrs.addFlashAttribute("success", "Traineeship deleted successfully");
        return "redirect:/traineeships";
    }

    @GetMapping("/my-applications")
    public String viewMyApplications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_TRAINEE) {
            return "redirect:/traineeships";
        }

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        List<Application> applications = applicationRepo.findByTraineeIdOrderByApplicationDateDesc(trainee.getId());
        
        List<ApplicationDto> applicationDtos = applications.stream()
            .map(app -> {
                ApplicationDto dto = new ApplicationDto();
                dto.setId(app.getId());
                dto.setApplicationDate(app.getApplicationDate());
                dto.setCoverLetter(app.getCoverLetter());
                dto.setComments(app.getComments());
                dto.setStatus(app.getStatus());
                
                // Add trainee's skills, interests and preferred location
                dto.setSkills(new ArrayList<>(trainee.getSkills()));
                dto.setInterests(new ArrayList<>(trainee.getInterests()));
                dto.setPreferredLocation(trainee.getPreferredLocation());
                
                if (app.getAssignedTraineeship() != null) {
                    TraineeshipDto traineeshipDto = convertToDto(app.getAssignedTraineeship());
                    dto.setTraineeship(traineeshipDto);
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        model.addAttribute("applications", applicationDtos);
        return "traineeship/my-applications";
    }

    @PostMapping("/applications/{id}/withdraw")
    public String withdrawApplication(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.ROLE_TRAINEE) {
            redirectAttrs.addFlashAttribute("error", "Only trainees can withdraw applications");
            return "redirect:/traineeships/my-applications";
        }

        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        if (!application.getTrainee().getId().equals(trainee.getId())) {
            redirectAttrs.addFlashAttribute("error", "You can only withdraw your own applications");
            return "redirect:/traineeships/my-applications";
        }

        if (application.getStatus() != Application.Status.PENDING) {
            redirectAttrs.addFlashAttribute("error", "You can only withdraw pending applications");
            return "redirect:/traineeships/my-applications";
        }

        applicationRepo.delete(application);
        redirectAttrs.addFlashAttribute("success", "Application withdrawn successfully");
        return "redirect:/traineeships/my-applications";
    }

    @GetMapping("/{id}/evaluate")
    public String showEvaluationForm(@PathVariable Long id, Model model, Authentication authentication) {
        // Verify company role and ownership
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_COMPANY) {
            return "redirect:/traineeships";
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        // Check if the company owns this traineeship
        Company company = companyRepo.findByUserId(user.getId());
        if (!company.getId().equals(traineeship.getCompany().getId())) {
            return "redirect:/traineeships";
        }

        // Check if the position is assigned
        if (traineeship.getStatus() != Traineeship.Status.FILLED) {
            return "redirect:/traineeships/" + id;
        }

        // Check if evaluation already exists
        Optional<Evaluation> existingEvaluation = evaluationRepo.findByTraineeshipIdAndEvaluatorType(
            id, Evaluation.EvaluatorType.COMPANY);
        if (existingEvaluation.isPresent()) {
            return "redirect:/traineeships/" + id + "?error=Evaluation already submitted";
        }

        TraineeshipDto dto = convertToDto(traineeship);
        model.addAttribute("traineeship", dto);
        return "traineeship/evaluate";
    }

    @PostMapping("/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
                                 @RequestParam int motivationRating,
                                 @RequestParam int effectivenessRating,
                                 @RequestParam int efficiencyRating,
                                 @RequestParam(required = false) String comments,
                                 RedirectAttributes redirectAttrs,
                                 Authentication authentication) {
        try {
            // Verify company role and ownership
            User user = userService.getCurrentUser(authentication);
            if (user.getRole() != User.Role.ROLE_COMPANY) {
                redirectAttrs.addFlashAttribute("error", "Only companies can submit evaluations");
                return "redirect:/traineeships";
            }

            Traineeship traineeship = traineeshipRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Traineeship not found"));
            
            // Check if the company owns this traineeship
            Company company = companyRepo.findByUserId(user.getId());
            if (!company.getId().equals(traineeship.getCompany().getId())) {
                redirectAttrs.addFlashAttribute("error", "You can only evaluate your own traineeships");
                return "redirect:/traineeships";
            }

            // Check if the position is assigned
            if (traineeship.getStatus() != Traineeship.Status.FILLED) {
                redirectAttrs.addFlashAttribute("error", "Can only evaluate assigned positions");
                return "redirect:/traineeships/" + id;
            }

            // Check if evaluation already exists
            Optional<Evaluation> existingEvaluation = evaluationRepo.findByTraineeshipIdAndEvaluatorType(
                id, Evaluation.EvaluatorType.COMPANY);
            if (existingEvaluation.isPresent()) {
                redirectAttrs.addFlashAttribute("error", "Evaluation already submitted");
                return "redirect:/traineeships/" + id;
            }

            // Validate ratings
            if (motivationRating < 1 || motivationRating > 5 ||
                effectivenessRating < 1 || effectivenessRating > 5 ||
                efficiencyRating < 1 || efficiencyRating > 5) {
                redirectAttrs.addFlashAttribute("error", "Ratings must be between 1 and 5");
                return "redirect:/traineeships/" + id + "/evaluate";
            }

            // Create and save evaluation
            Evaluation evaluation = new Evaluation();
            evaluation.setTraineeship(traineeship);
            evaluation.setEvaluatorType(Evaluation.EvaluatorType.COMPANY);
            evaluation.setMotivationRating(motivationRating);
            evaluation.setEffectivenessRating(effectivenessRating);
            evaluation.setEfficiencyRating(efficiencyRating);
            evaluation.setFacilitiesRating(5); // Default value for company evaluations
            evaluation.setGuidanceRating(5); // Default value for company evaluations
            evaluation.setComments(comments != null ? comments : "");
            evaluation.setDate(LocalDate.now());
            evaluation.setEvaluator(user); // Set the evaluator
            
            evaluationRepo.save(evaluation);

            redirectAttrs.addFlashAttribute("success", "Evaluation submitted successfully");
            return "redirect:/traineeships/" + id;
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Failed to submit evaluation: " + e.getMessage());
            return "redirect:/traineeships/" + id;
        }
    }

    @GetMapping("/{id}/assign-supervisor")
    public String assignSupervisorForm(@PathVariable Long id, Model model) {
        Traineeship traineeship = traineeshipService.getTraineeshipById(id);
        if (traineeship == null) {
            return "redirect:/traineeships";
        }

        // Get suitable professors with their scores
        List<ProfessorProfile> professors = traineeshipService.findSuitableProfessors(traineeship);
        
        // Calculate scores for each professor
        for (ProfessorProfile professor : professors) {
            // Calculate load score (0-1, where 1 is best - no load)
            int currentLoad = professor.getSupervisedTraineeships().stream()
                .filter(t -> t.getStatus() == Traineeship.Status.FILLED)
                .collect(Collectors.toList())
                .size();
            double loadScore = Math.max(0, 1 - (currentLoad / 5.0)); // Max 5 traineeships

            // Calculate interest match score
            Set<String> traineeshipInterests = new HashSet<>(traineeship.getRequiredSkills());
            Set<String> professorInterests = new HashSet<>(professor.getInterests());
            double interestScore = calculateInterestMatchScore(traineeshipInterests, professorInterests);

            // Calculate overall suitability score (weighted average)
            double suitabilityScore = (loadScore * 0.4) + (interestScore * 0.6);

            // Set scores on the professor object
            professor.setLoadScore(loadScore);
            professor.setInterestScore(interestScore);
            professor.setSuitabilityScore(suitabilityScore);
        }

        // Sort professors by suitability score (highest first)
        professors.sort((p1, p2) -> Double.compare(p2.getSuitabilityScore(), p1.getSuitabilityScore()));

        model.addAttribute("traineeship", traineeship);
        model.addAttribute("professors", professors);
        return "traineeship/assign-supervisor";
    }

    @PostMapping("/{id}/assign-supervisor")
    public String assignSupervisor(@PathVariable Long id, 
                                 @RequestParam Long professorId,
                                 RedirectAttributes redirectAttrs) {
        try {
            traineeshipService.assignSupervisor(id, professorId);
            redirectAttrs.addFlashAttribute("success", "Supervisor assigned successfully");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Failed to assign supervisor: " + e.getMessage());
        }
        return "redirect:/traineeships/" + id;
    }

    private double calculateInterestMatchScore(Set<String> traineeshipInterests, Set<String> professorInterests) {
        if (professorInterests.isEmpty() || traineeshipInterests.isEmpty()) {
            return 0.0;
        }

        // Convert all strings to lowercase for case-insensitive matching
        Set<String> lowerTraineeshipInterests = traineeshipInterests.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        Set<String> lowerProfessorInterests = professorInterests.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // Count matching interests
        long matches = lowerProfessorInterests.stream()
            .filter(lowerTraineeshipInterests::contains)
            .count();

        // Calculate score based on number of matches relative to total interests
        return (double) matches / Math.max(lowerTraineeshipInterests.size(), lowerProfessorInterests.size());
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
            dto.setAssignedTraineeStudentId(traineeship.getAssignedTrainee().getStudentId());
            dto.setAssigned(true);
        }
        
        if (traineeship.getSupervisor() != null) {
            dto.setSupervisorId(traineeship.getSupervisor().getId());
            dto.setSupervisorName(traineeship.getSupervisor().getFullName());
        }

        // Check if company evaluation exists
        boolean hasCompanyEvaluation = evaluationRepo
            .findByTraineeshipIdAndEvaluatorType(traineeship.getId(), Evaluation.EvaluatorType.COMPANY)
            .isPresent();
        dto.setHasEvaluation(hasCompanyEvaluation);

        // Add evaluation information if it exists
        if (hasCompanyEvaluation) {
            evaluationRepo.findByTraineeshipIdAndEvaluatorType(traineeship.getId(), Evaluation.EvaluatorType.COMPANY)
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

        // Add completion information
        dto.setCompletionOutcome(traineeship.getCompletionOutcome());
        dto.setCompletionComments(traineeship.getCompletionComments());
        dto.setCompletionDate(traineeship.getCompletionDate());

        return dto;
    }

    @PostMapping("/{id}/complete")
    public String completeTraineeship(@PathVariable Long id,
                                    @RequestParam String outcome,
                                    @RequestParam(required = false) String comments,
                                    RedirectAttributes redirectAttrs,
                                    Authentication authentication) {
        try {
            // Verify committee role
            User user = userService.getCurrentUser(authentication);
            if (user.getRole() != User.Role.ROLE_COMMITTEE) {
                redirectAttrs.addFlashAttribute("error", "Only committee members can complete traineeships");
                return "redirect:/traineeships";
            }

            Traineeship traineeship = traineeshipRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Traineeship not found"));

            // Verify all conditions are met
            if (traineeship.getStatus() != Traineeship.Status.FILLED) {
                redirectAttrs.addFlashAttribute("error", "Traineeship must be filled to be completed");
                return "redirect:/traineeships";
            }
            if (traineeship.getSupervisor() == null) {
                redirectAttrs.addFlashAttribute("error", "Traineeship must have a supervisor assigned");
                return "redirect:/traineeships";
            }
            if (!traineeship.hasEvaluation()) {
                redirectAttrs.addFlashAttribute("error", "Traineeship must be evaluated before completion");
                return "redirect:/traineeships";
            }

            // Update traineeship status
            traineeship.setStatus(Traineeship.Status.COMPLETED);
            traineeship.setCompletionOutcome(outcome);
            traineeship.setCompletionComments(comments);
            traineeship.setCompletionDate(LocalDate.now());
            traineeshipRepo.save(traineeship);

            redirectAttrs.addFlashAttribute("success", "Traineeship completed successfully with outcome: " + outcome);
            return "redirect:/traineeships";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Failed to complete traineeship: " + e.getMessage());
            return "redirect:/traineeships";
        }
    }
} 