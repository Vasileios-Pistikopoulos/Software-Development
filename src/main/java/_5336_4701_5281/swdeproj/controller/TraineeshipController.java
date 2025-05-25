package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.TraineeshipDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
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

@Controller
@RequestMapping("/traineeships")
public class TraineeshipController {

    private final TraineeshipRepository traineeshipRepo;
    private final ApplicationRepository applicationRepo;
    private final UserRepository userRepo;
    private final TraineeProfileRepository traineeRepo;
    private final CompanyRepository companyRepo;

    public TraineeshipController(TraineeshipRepository traineeshipRepo,
                                ApplicationRepository applicationRepo,
                                UserRepository userRepo,
                                TraineeProfileRepository traineeRepo,
                                CompanyRepository companyRepo) {
        this.traineeshipRepo = traineeshipRepo;
        this.applicationRepo = applicationRepo;
        this.userRepo = userRepo;
        this.traineeRepo = traineeRepo;
        this.companyRepo = companyRepo;
    }

    @GetMapping
    public String listTraineeships(@RequestParam(required = false) String location,
                                 @RequestParam(required = false) String skill,
                                 @RequestParam(required = false) String topic,
                                 Model model) {
        List<Traineeship> traineeships = traineeshipRepo.findByFilters(location, skill, topic);
        List<TraineeshipDto> dtos = traineeships.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        model.addAttribute("traineeships", dtos);
        model.addAttribute("location", location);
        model.addAttribute("skill", skill);
        model.addAttribute("topic", topic);
        return "traineeship/list";
    }

    @GetMapping("/{id}")
    public String viewTraineeship(@PathVariable Long id, Model model) {
        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        TraineeshipDto dto = convertToDto(traineeship);
        model.addAttribute("traineeship", dto);

        // Check if current user has already applied
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            User user = userRepo.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if ("TRAINEE".equals(user.getRole())) {
                TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
                boolean hasApplied = applicationRepo.findByTraineeIdAndTraineeshipId(
                        trainee.getId(), id).isPresent();
                model.addAttribute("hasApplied", hasApplied);
            }
        }

        return "traineeship/view";
    }

    @PostMapping("/{id}/apply")
    public String applyForTraineeship(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!"TRAINEE".equals(user.getRole())) {
            redirectAttrs.addFlashAttribute("error", "Only trainees can apply for traineeships");
            return "redirect:/traineeships/" + id;
        }

        Traineeship traineeship = traineeshipRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        
        // Check if already applied
        if (applicationRepo.findByTraineeIdAndTraineeshipId(trainee.getId(), id).isPresent()) {
            redirectAttrs.addFlashAttribute("error", "You have already applied for this traineeship");
            return "redirect:/traineeships/" + id;
        }

        // Create new application
        Application application = new Application(trainee, traineeship);
        applicationRepo.save(application);

        redirectAttrs.addFlashAttribute("success", "Application submitted successfully");
        return "redirect:/traineeships/" + id;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getRole().equals(User.Role.ROLE_COMPANY)) {
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
        
        if (!user.getRole().equals(User.Role.ROLE_COMPANY)) {
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

    private TraineeshipDto convertToDto(Traineeship traineeship) {
        TraineeshipDto dto = new TraineeshipDto();
        dto.setId(traineeship.getId());
        dto.setTitle(traineeship.getTitle());
        dto.setDescription(traineeship.getDescription());
        dto.setStartDate(traineeship.getStartDate());
        dto.setEndDate(traineeship.getEndDate());
        dto.setSkillsRequired(new ArrayList<>(traineeship.getRequiredSkills()));
        dto.setTopics(new ArrayList<>(traineeship.getTopics()));
        dto.setLocation(traineeship.getLocation());
        dto.setCompanyId(traineeship.getCompany().getId());
        dto.setCompanyName(traineeship.getCompany().getName());
        
        if (traineeship.getAssignedTrainee() != null) {
            dto.setAssignedTraineeId(traineeship.getAssignedTrainee().getId());
            dto.setAssignedTraineeName(traineeship.getAssignedTrainee().getFullName());
            dto.setAssigned(true);
        }
        
        if (traineeship.getSupervisor() != null) {
            dto.setSupervisorId(traineeship.getSupervisor().getId());
            dto.setSupervisorName(traineeship.getSupervisor().getFullName());
        }

        return dto;
    }
} 