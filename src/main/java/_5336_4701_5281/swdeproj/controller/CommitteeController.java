package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.ApplicationDto;
import _5336_4701_5281.swdeproj.dto.TraineeshipDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.service.NotificationService;
import _5336_4701_5281.swdeproj.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/committee")
public class CommitteeController {

    private final ApplicationRepository applicationRepo;
    private final TraineeshipRepository traineeshipRepo;
    private final UserRepository userRepo;
    private final EvaluationRepository evaluationRepo;
    private final LogbookEntryRepository logbookRepo;
    private final NotificationService notificationService;
    private final UserService userService;

    public CommitteeController(ApplicationRepository applicationRepo,
                             TraineeshipRepository traineeshipRepo,
                             UserRepository userRepo,
                             EvaluationRepository evaluationRepo,
                             LogbookEntryRepository logbookRepo,
                             NotificationService notificationService,
                             UserService userService) {
        this.applicationRepo = applicationRepo;
        this.traineeshipRepo = traineeshipRepo;
        this.userRepo = userRepo;
        this.evaluationRepo = evaluationRepo;
        this.logbookRepo = logbookRepo;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/applications")
    public String listApplications(Model model, Authentication authentication) {
        // Verify committee member role
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_COMMITTEE) {
            return "redirect:/";
        }

        List<Application> applications = applicationRepo.findByStatus(Application.Status.PENDING);
        List<ApplicationDto> applicationDtos = applications.stream()
            .map(app -> {
                ApplicationDto dto = new ApplicationDto();
                dto.setId(app.getId());
                dto.setApplicationDate(app.getApplicationDate());
                dto.setCoverLetter(app.getCoverLetter());
                dto.setStatus(app.getStatus());
                
                TraineeProfile trainee = app.getTrainee();
                dto.setTraineeName(trainee.getFullName());
                dto.setStudentId(trainee.getStudentId());
                dto.setSkills(new ArrayList<>(trainee.getSkills()));
                dto.setInterests(new ArrayList<>(trainee.getInterests()));
                dto.setPreferredLocation(trainee.getPreferredLocation());
                
                return dto;
            })
            .collect(Collectors.toList());
        
        model.addAttribute("applications", applicationDtos);
        return "committee/applications";
    }

    @GetMapping({"/applications/{id}/search-traineeships", "/search-traineeships/{id}"})
    public String searchTraineeships(@PathVariable Long id,
                                   @RequestParam(required = false, defaultValue = "ALL") String searchCriteria,
                                   Model model,
                                   Authentication authentication) {
        try {
            // Verify committee member role
            User user = userService.getCurrentUser(authentication);
            if (user.getRole() != User.Role.ROLE_COMMITTEE) {
                return "redirect:/";
            }

            // Fetch application with trainee data and all related collections in a single query
            Application application = applicationRepo.findByIdWithTraineeAndCollections(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
            
            TraineeProfile trainee = application.getTrainee();
            if (trainee == null) {
                throw new RuntimeException("Trainee profile not found for application");
            }
            
            // Convert application to DTO with all necessary information
            ApplicationDto applicationDto = new ApplicationDto();
            applicationDto.setId(application.getId());
            applicationDto.setApplicationDate(application.getApplicationDate());
            applicationDto.setCoverLetter(application.getCoverLetter());
            applicationDto.setComments(application.getComments());
            applicationDto.setStatus(application.getStatus());
            
            // Set trainee information - collections are already loaded
            applicationDto.setTraineeName(trainee.getFullName());
            applicationDto.setStudentId(trainee.getStudentId());
            applicationDto.setSkills(new ArrayList<>(trainee.getSkills()));
            applicationDto.setInterests(new ArrayList<>(trainee.getInterests()));
            applicationDto.setPreferredLocation(trainee.getPreferredLocation());
            
            // Get all open traineeships with their collections loaded
            List<Traineeship> allTraineeships = traineeshipRepo.findByStatusWithCollections(Traineeship.Status.OPEN);
            
            // Create a list of traineeships with their match scores
            List<Map<String, Object>> traineeshipsWithScores = allTraineeships.stream()
                .map(t -> {
                    Map<String, Object> traineeshipInfo = new HashMap<>();
                    TraineeshipDto dto = convertToDto(t);
                    traineeshipInfo.put("traineeship", dto);
                    
                    // Calculate match scores using the trainee's skills and interests
                    Set<String> traineeSkills = trainee.getSkills().stream()
                        .map(String::trim)
                        .collect(Collectors.toSet());
                    Set<String> traineeInterests = trainee.getInterests().stream()
                        .map(String::trim)
                        .collect(Collectors.toSet());
                    Set<String> requiredSkills = t.getRequiredSkills().stream()
                        .map(String::trim)
                        .collect(Collectors.toSet());
                    Set<String> topics = t.getTopics().stream()
                        .map(String::trim)
                        .collect(Collectors.toSet());
                    
                    // Count all matching skills and interests
                    List<String> matchingSkills = new ArrayList<>();
                    for (String skill : requiredSkills) {
                        if (traineeSkills.contains(skill)) {
                            matchingSkills.add(skill);
                        }
                    }
                    
                    List<String> matchingInterests = new ArrayList<>();
                    for (String topic : topics) {
                        if (traineeInterests.contains(topic)) {
                            matchingInterests.add(topic);
                        }
                    }
                    
                    int skillMatches = matchingSkills.size();
                    int interestMatches = matchingInterests.size();
                    boolean locationMatch = trainee.getPreferredLocation() != null && 
                        trainee.getPreferredLocation().trim().equalsIgnoreCase(t.getLocation().trim());
                    
                    // Calculate score based on selected search criteria
                    double score = 0.0;
                    switch (searchCriteria) {
                        case "SKILLS":
                            // Only consider skills matches
                            score = skillMatches * 2.0;
                            break;
                        case "INTERESTS":
                            // Only consider interests matches
                            score = interestMatches * 2.0;
                            break;
                        case "LOCATION":
                            // Only consider location match
                            score = locationMatch ? 2.0 : 0.0;
                            break;
                        case "SKILLS_AND_INTERESTS":
                            // Consider both skills and interests equally
                            score = (skillMatches + interestMatches) * 2.0;
                            break;
                        case "SKILLS_AND_LOCATION":
                            // Consider skills and location equally
                            score = (skillMatches * 2.0) + (locationMatch ? 2.0 : 0.0);
                            break;
                        case "INTERESTS_AND_LOCATION":
                            // Consider interests and location equally
                            score = (interestMatches * 2.0) + (locationMatch ? 2.0 : 0.0);
                            break;
                        case "ALL":
                        default:
                            // Consider all factors with different weights
                            score = (skillMatches * 2.0) + // Skills are most important
                                   (interestMatches * 1.5) + // Interests are second
                                   (locationMatch ? 1.0 : 0.0); // Location is third
                            break;
                    }
                    
                    // Store match information
                    Map<String, Object> matches = new HashMap<>();
                    matches.put("skillMatches", skillMatches);
                    matches.put("interestMatches", interestMatches);
                    matches.put("locationMatch", locationMatch);
                    matches.put("matchingSkills", matchingSkills);
                    matches.put("matchingInterests", matchingInterests);
                    
                    traineeshipInfo.put("score", score);
                    traineeshipInfo.put("matches", matches);
                    
                    return traineeshipInfo;
                })
                .filter(info -> {
                    // Always filter out traineeships where the student has no matching required skills
                    Map<String, Object> matches = (Map<String, Object>) info.get("matches");
                    return ((Integer) matches.get("skillMatches")) > 0;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score"))) // Sort by score descending
                .collect(Collectors.toList());
            
            model.addAttribute("application", application);
            model.addAttribute("applicationDto", applicationDto);
            model.addAttribute("traineeshipsWithScores", traineeshipsWithScores);
            model.addAttribute("selectedCriteria", searchCriteria);
            model.addAttribute("traineeInterests", new ArrayList<>(trainee.getInterests()));
            model.addAttribute("traineeLocation", trainee.getPreferredLocation());
            
            return "committee/search-traineeships";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            model.addAttribute("error", "An error occurred while loading the page: " + e.getMessage());
            return "redirect:/committee/applications";
        }
    }

    @PostMapping("/assign-traineeship")
    public String assignTraineeship(@RequestParam(required = false) String applicationId,
                                  @RequestParam(required = false) String traineeshipId,
                                  RedirectAttributes redirectAttrs,
                                  Authentication authentication) {
        // Debug logging
        System.out.println("=== Debug: Assign Traineeship Parameters ===");
        System.out.println("Raw Application ID: '" + applicationId + "'");
        System.out.println("Raw Traineeship ID: '" + traineeshipId + "'");
        System.out.println("Application ID is null: " + (applicationId == null));
        System.out.println("Traineeship ID is null: " + (traineeshipId == null));
        System.out.println("Application ID is empty: " + (applicationId != null && applicationId.trim().isEmpty()));
        System.out.println("Traineeship ID is empty: " + (traineeshipId != null && traineeshipId.trim().isEmpty()));

        // Verify committee member role
        User user = userService.getCurrentUser(authentication);
        if (user.getRole() != User.Role.ROLE_COMMITTEE) {
            redirectAttrs.addFlashAttribute("error", "Only committee members can assign traineeships");
            return "redirect:/";
        }

        try {
            // Check for null or empty parameters
            if (applicationId == null || traineeshipId == null) {
                System.out.println("Error: One or both parameters are null");
                redirectAttrs.addFlashAttribute("error", "Missing required parameters: applicationId or traineeshipId is null");
                return "redirect:/committee/applications";
            }

            if (applicationId.trim().isEmpty() || traineeshipId.trim().isEmpty()) {
                System.out.println("Error: One or both parameters are empty strings");
                redirectAttrs.addFlashAttribute("error", "Missing required parameters: applicationId or traineeshipId is empty");
                return "redirect:/committee/applications";
            }

            Long appId = Long.parseLong(applicationId.trim());
            Long traineeshipIdLong = Long.parseLong(traineeshipId.trim());

            System.out.println("Parsed Application ID: " + appId);
            System.out.println("Parsed Traineeship ID: " + traineeshipIdLong);

            Application application = applicationRepo.findById(appId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
            
            Traineeship traineeship = traineeshipRepo.findById(traineeshipIdLong)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
            
            // Check if traineeship is still available
            if (traineeship.getStatus() != Traineeship.Status.OPEN) {
                redirectAttrs.addFlashAttribute("error", "This traineeship is no longer available");
                return "redirect:/committee/applications/" + appId + "/search-traineeships";
            }

            // Check if application is still pending
            if (application.getStatus() != Application.Status.PENDING) {
                redirectAttrs.addFlashAttribute("error", "This application is no longer pending");
                return "redirect:/committee/applications/" + appId + "/search-traineeships";
            }
            
            // Update application status and assign traineeship
            application.setStatus(Application.Status.APPROVED);
            application.setAssignedTraineeship(traineeship);
            applicationRepo.save(application);
            
            // Update traineeship status
            traineeship.setStatus(Traineeship.Status.FILLED);
            traineeship.setAssignedTrainee(application.getTrainee());
            traineeshipRepo.save(traineeship);
            
            // Send notification to trainee
            notificationService.notifyApplicationStatusChange(application);
            
            redirectAttrs.addFlashAttribute("success", "Traineeship assigned successfully");
            return "redirect:/committee/applications";
            
        } catch (NumberFormatException e) {
            e.printStackTrace(); // Log the error
            redirectAttrs.addFlashAttribute("error", "Invalid ID format: " + e.getMessage());
            return "redirect:/committee/applications";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            redirectAttrs.addFlashAttribute("error", "Failed to assign traineeship: " + e.getMessage());
            return "redirect:/committee/applications";
        }
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

    @GetMapping("/applications/{id}/search-matches")
    public String searchMatchingTraineeships(@PathVariable Long id,
                                           @RequestParam(required = false) String interests,
                                           @RequestParam(required = false) String location,
                                           Model model,
                                           Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        if (!user.getRole().equals(User.Role.ROLE_COMMITTEE)) {
            return "redirect:/";
        }

        // Get the student's application
        Application application = applicationRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        // Get the student's profile
        TraineeProfile studentProfile = application.getTrainee();
        if (studentProfile == null) {
            throw new RuntimeException("Student profile not found");
        }
        
        // Get student's skills and interests
        Set<String> studentSkills = studentProfile.getSkills();
        Set<String> studentInterests = studentProfile.getInterests();
        String preferredLocation = studentProfile.getPreferredLocation();
        
        // Use provided search parameters or fall back to student's preferences
        String searchLocation = location != null ? location : preferredLocation;
        String searchInterests = interests != null ? interests : 
            (studentInterests != null && !studentInterests.isEmpty() ? studentInterests.iterator().next() : null);
        
        // Search for matching traineeships
        List<Traineeship> matchingTraineeships = traineeshipRepo.findByFilters(
            searchLocation,
            studentSkills != null && !studentSkills.isEmpty() ? studentSkills.iterator().next() : null,
            searchInterests
        );
        
        // Convert to DTOs
        List<TraineeshipDto> matchingDtos = matchingTraineeships.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        model.addAttribute("student", studentProfile);
        model.addAttribute("currentApplication", convertToDto(application));
        model.addAttribute("matchingTraineeships", matchingDtos);
        model.addAttribute("searchInterests", searchInterests);
        model.addAttribute("searchLocation", searchLocation);
        
        return "committee/matching-traineeships";
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
        
        return dto;
    }

    private ApplicationDto convertToDto(Application application) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setCoverLetter(application.getCoverLetter());
        dto.setComments(application.getComments());
        dto.setStatus(application.getStatus());
        
        TraineeProfile trainee = application.getTrainee();
        dto.setTraineeName(trainee.getFullName());
        dto.setStudentId(trainee.getStudentId());
        dto.setSkills(new ArrayList<>(trainee.getSkills()));
        dto.setInterests(new ArrayList<>(trainee.getInterests()));
        dto.setPreferredLocation(trainee.getPreferredLocation());
        
        if (application.getAssignedTraineeship() != null) {
            dto.setTraineeship(convertToDto(application.getAssignedTraineeship()));
        }
        
        return dto;
    }
} 