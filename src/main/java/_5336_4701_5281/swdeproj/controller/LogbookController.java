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
    public String viewLogbook(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                            @RequestParam(required = false) String status,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals(User.Role.ROLE_TRAINEE)) {
            return "redirect:/home";
        }

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        if (trainee == null) {
            redirectAttrs.addFlashAttribute("error", "Trainee profile not found");
            return "redirect:/home";
        }

        // Find the active traineeship
        Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(trainee.getId());

        // Set default dates if not provided
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        // Get entries based on filters
        List<LogbookEntry> entries;
        if (status != null && !status.isEmpty()) {
            entries = logbookRepo.findByTraineeIdAndDateBetweenAndStatus(
                trainee.getId(), startDate, endDate, LogbookEntry.Status.valueOf(status));
        } else {
            entries = logbookRepo.findByTraineeIdAndDateBetween(trainee.getId(), startDate, endDate);
        }

        // Calculate statistics
        Double totalHours = logbookRepo.sumHoursByTraineeAndTraineeship(trainee.getId(), activeTraineeship.getId());
        Double approvedHours = logbookRepo.sumHoursByTraineeAndTraineeshipAndStatus(
            trainee.getId(), activeTraineeship.getId(), LogbookEntry.Status.APPROVED);

        model.addAttribute("entries", entries);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
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

        if (!user.getRole().equals(User.Role.ROLE_TRAINEE)) {
            return "redirect:/home";
        }

        TraineeProfile trainee = traineeRepo.findByUserId(user.getId());
        if (trainee == null) {
            redirectAttrs.addFlashAttribute("error", "Trainee profile not found");
            return "redirect:/home";
        }

        Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(trainee.getId());
        if (activeTraineeship == null || 
            activeTraineeship.getStatus() != Traineeship.Status.FILLED || 
            LocalDate.now().isBefore(activeTraineeship.getStartDate()) || 
            LocalDate.now().isAfter(activeTraineeship.getEndDate())) {
            redirectAttrs.addFlashAttribute("error", "You don't have an active traineeship. Logbook entries can only be created during an active traineeship.");
            return "redirect:/home";
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

        if (!user.getRole().equals(User.Role.ROLE_TRAINEE)) {
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