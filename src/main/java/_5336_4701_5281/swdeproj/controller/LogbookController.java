package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/logbook")
public class LogbookController {

    private final LogbookEntryRepository logbookRepo;
    private final UserRepository userRepo;
    private final TraineeProfileRepository traineeRepo;
    private final TraineeshipRepository traineeshipRepo;

    public LogbookController(LogbookEntryRepository logbookRepo,
                           UserRepository userRepo,
                           TraineeProfileRepository traineeRepo,
                           TraineeshipRepository traineeshipRepo) {
        this.logbookRepo = logbookRepo;
        this.userRepo = userRepo;
        this.traineeRepo = traineeRepo;
        this.traineeshipRepo = traineeshipRepo;
    }

    @GetMapping
    public String viewLogbook(Model model,
                            RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.hasRole(User.Role.ROLE_TRAINEE)) {
            return "redirect:/home";
        }

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        if (trainee == null) {
            redirectAttrs.addFlashAttribute("error", "Trainee profile not found");
            return "redirect:/home";
        }

        // Find the active traineeship
        Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(trainee.getId());
        if (activeTraineeship == null) {
            redirectAttrs.addFlashAttribute("error", "You don't have an active traineeship. Please wait for a traineeship to be assigned to you.");
            return "redirect:/home";
        }

        if (activeTraineeship.getStatus() != Traineeship.Status.FILLED) {
            redirectAttrs.addFlashAttribute("error", "Your traineeship is not active yet. Please wait for it to be filled.");
            return "redirect:/home";
        }

        // Get all entries
        List<LogbookEntry> entries = logbookRepo.findByTraineeId(trainee.getUser().getId());

        // Calculate statistics
        Double totalHours = logbookRepo.sumHoursByTraineeAndTraineeship(trainee.getId(), activeTraineeship.getId());
        Double approvedHours = logbookRepo.sumHoursByTraineeAndTraineeshipAndStatus(
            trainee.getId(), activeTraineeship.getId(), LogbookEntry.Status.APPROVED);

        model.addAttribute("entries", entries);
        model.addAttribute("activeTraineeship", activeTraineeship);
        model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
        model.addAttribute("approvedHours", approvedHours != null ? approvedHours : 0.0);

        return "logbook/view";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Debug - User ID: " + user.getId());
        System.out.println("Debug - User Role: " + user.getRole());
        System.out.println("Debug - User Roles: " + user.getRoles());

        if (!user.hasRole(User.Role.ROLE_TRAINEE)) {
            System.out.println("Debug - User does not have ROLE_TRAINEE");
            return "redirect:/home";
        }

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        System.out.println("Debug - Trainee Profile: " + (trainee != null ? "Found" : "Not Found"));
        
        if (trainee == null) {
            System.out.println("Debug - Trainee profile not found for user ID: " + user.getId());
            redirectAttrs.addFlashAttribute("error", "Trainee profile not found");
            return "redirect:/logbook";
        }

        Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(trainee.getId());
        System.out.println("Debug - Active Traineeship: " + (activeTraineeship != null ? "Found" : "Not Found"));
        
        if (activeTraineeship == null) {
            System.out.println("Debug - No active traineeship found");
            redirectAttrs.addFlashAttribute("error", "You don't have an active traineeship. Please wait for a traineeship to be assigned to you.");
            return "redirect:/logbook";
        }

        if (activeTraineeship.getStatus() != Traineeship.Status.FILLED) {
            System.out.println("Debug - Traineeship status is not FILLED: " + activeTraineeship.getStatus());
            redirectAttrs.addFlashAttribute("error", "Your traineeship is not active yet. Please wait for it to be filled.");
            return "redirect:/logbook";
        }

        if (LocalDate.now().isBefore(activeTraineeship.getStartDate())) {
            System.out.println("Debug - Current date is before traineeship start date");
            redirectAttrs.addFlashAttribute("error", "Your traineeship hasn't started yet. It will begin on " + 
                activeTraineeship.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            return "redirect:/logbook";
        }

        if (LocalDate.now().isAfter(activeTraineeship.getEndDate())) {
            System.out.println("Debug - Current date is after traineeship end date");
            redirectAttrs.addFlashAttribute("error", "Your traineeship has ended on " + 
                activeTraineeship.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            return "redirect:/logbook";
        }

        LogbookEntry entry = new LogbookEntry();
        entry.setDate(LocalDate.now());
        model.addAttribute("logbookEntry", entry);
        model.addAttribute("activeTraineeship", activeTraineeship);
        return "logbook/create";
    }

    @PostMapping("/create")
    public String createEntry(@ModelAttribute LogbookEntry entry, RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(trainee.getId());

        if (activeTraineeship == null || 
            activeTraineeship.getStatus() != Traineeship.Status.FILLED || 
            LocalDate.now().isBefore(activeTraineeship.getStartDate()) || 
            LocalDate.now().isAfter(activeTraineeship.getEndDate())) {
            redirectAttrs.addFlashAttribute("error", "No active traineeship found");
            return "redirect:/logbook";
        }

        // Validate entry date is within traineeship period
        if (entry.getDate() != null) {
            if (entry.getDate().isBefore(activeTraineeship.getStartDate()) || 
                entry.getDate().isAfter(activeTraineeship.getEndDate())) {
                redirectAttrs.addFlashAttribute("error", "Entry date must be within the traineeship period");
                return "redirect:/logbook/create";
            }
        } else {
            entry.setDate(LocalDate.now());
        }

        entry.setTrainee(trainee.getUser());
        entry.setTraineeship(activeTraineeship);
        entry.setStatus(LogbookEntry.Status.PENDING);

        logbookRepo.save(entry);
        redirectAttrs.addFlashAttribute("success", "Logbook entry created successfully");
        return "redirect:/logbook";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        LogbookEntry entry = logbookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());

        if (!entry.getTrainee().getId().equals(trainee.getId())) {
            return "redirect:/logbook";
        }

        model.addAttribute("logbookEntry", entry);
        return "logbook/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateEntry(@PathVariable Long id, 
                            @ModelAttribute LogbookEntry updatedEntry,
                            RedirectAttributes redirectAttrs) {
        LogbookEntry entry = logbookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());

        if (!entry.getTrainee().getId().equals(trainee.getId())) {
            return "redirect:/logbook";
        }

        entry.setDate(updatedEntry.getDate());
        entry.setTaskDescription(updatedEntry.getTaskDescription());
        entry.setHoursSpent(updatedEntry.getHoursSpent());
        entry.setLearningOutcomes(updatedEntry.getLearningOutcomes());

        logbookRepo.save(entry);
        redirectAttrs.addFlashAttribute("success", "Logbook entry updated successfully");
        return "redirect:/logbook";
    }

    @PostMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        LogbookEntry entry = logbookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());

        if (!entry.getTrainee().getId().equals(trainee.getId())) {
            return "redirect:/logbook";
        }

        logbookRepo.delete(entry);
        redirectAttrs.addFlashAttribute("success", "Logbook entry deleted successfully");
        return "redirect:/logbook";
    }

    @PostMapping("/{id}/approve")
    public String approveEntry(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        LogbookEntry entry = logbookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only professors can approve entries
        if (!user.hasRole(User.Role.ROLE_PROFESSOR)) {
            return "redirect:/logbook";
        }

        // Verify the professor is the supervisor for this traineeship
        if (!entry.getTraineeship().getSupervisor().getId().equals(user.getId())) {
            redirectAttrs.addFlashAttribute("error", "Not authorized to approve this entry");
            return "redirect:/logbook";
        }

        entry.setStatus(LogbookEntry.Status.APPROVED);
        logbookRepo.save(entry);

        redirectAttrs.addFlashAttribute("success", "Logbook entry approved successfully");
        return "redirect:/logbook";
    }

    @PostMapping("/{id}/reject")
    public String rejectEntry(@PathVariable Long id, 
                            @RequestParam(required = false) String reason,
                            RedirectAttributes redirectAttrs) {
        LogbookEntry entry = logbookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only professors can reject entries
        if (!user.hasRole(User.Role.ROLE_PROFESSOR)) {
            return "redirect:/logbook";
        }

        // Verify the professor is the supervisor for this traineeship
        if (!entry.getTraineeship().getSupervisor().getId().equals(user.getId())) {
            redirectAttrs.addFlashAttribute("error", "Not authorized to reject this entry");
            return "redirect:/logbook";
        }

        entry.setStatus(LogbookEntry.Status.REJECTED);
        if (reason != null && !reason.trim().isEmpty()) {
            entry.setComments(reason);
        }
        logbookRepo.save(entry);

        redirectAttrs.addFlashAttribute("success", "Logbook entry rejected successfully");
        return "redirect:/logbook";
    }

    @GetMapping("/statistics")
    public String viewStatistics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.hasRole(User.Role.ROLE_TRAINEE)) {
            return "redirect:/home";
        }

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(trainee.getId());

        if (activeTraineeship == null) {
            return "redirect:/logbook";
        }

        // Calculate statistics
        Double totalHours = logbookRepo.sumHoursByTraineeAndTraineeship(
            trainee.getId(), activeTraineeship.getId());
        Long approvedEntries = logbookRepo.countApprovedEntriesByTraineeAndTraineeship(
            trainee.getId(), activeTraineeship.getId(), LogbookEntry.Status.APPROVED);

        model.addAttribute("totalHours", totalHours != null ? totalHours : 0.0);
        model.addAttribute("approvedEntries", approvedEntries);
        model.addAttribute("traineeship", activeTraineeship);

        return "logbook/statistics";
    }
} 