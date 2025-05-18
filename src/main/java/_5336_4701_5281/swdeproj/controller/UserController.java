package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.UserRegistrationDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final TraineeProfileRepository traineeRepo;
    private final CompanyProfileRepository companyRepo;
    private final ProfessorProfileRepository professorRepo;
    private final CommitteeProfileRepository committeeRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          TraineeProfileRepository traineeRepo,
                          CompanyProfileRepository companyRepo,
                          ProfessorProfileRepository professorRepo,
                          CommitteeProfileRepository committeeRepo,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.traineeRepo = traineeRepo;
        this.companyRepo = companyRepo;
        this.professorRepo = professorRepo;
        this.committeeRepo = committeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "signup";
    }

    @PostMapping("/register")
    @Transactional
    public String registerUser(@ModelAttribute("userDto") UserRegistrationDto dto, Model model) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }

        User user = new User(
                dto.getUsername(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getRole()
        );

        switch (dto.getRole()) {
            case "TRAINEE" -> {
                TraineeProfile profile = new TraineeProfile();
                profile.setFullName(dto.getTraineeFullName());
                profile.setStudentId(dto.getStudentId());
                profile.setSkills(splitAndTrim(dto.getTraineeSkills()));
                profile.setInterests(splitAndTrim(dto.getTraineeInterests()));
                profile.setPreferredLocation(dto.getPreferredLocation());

                // σύνδεση
                profile.setUser(user);
                user.setTraineeProfile(profile);
            }
            case "COMPANY" -> {
                CompanyProfile profile = new CompanyProfile();
                profile.setCompanyName(dto.getCompanyName());
                profile.setLocation(dto.getCompanyLocation());

                profile.setUser(user);
                user.setCompanyProfile(profile);
            }
            case "PROFESSOR" -> {
                ProfessorProfile profile = new ProfessorProfile();
                profile.setFullName(dto.getProfessorFullName());
                profile.setInterests(splitAndTrim(dto.getProfessorInterests()));

                profile.setUser(user);
                user.setProfessorProfile(profile);
            }
            case "COMMITTEE" -> {
                CommitteeProfile profile = new CommitteeProfile();
                profile.setUser(user);
                user.setCommitteeProfile(profile);
            }
            default -> {
                model.addAttribute("error", "Invalid role selected");
                return "signup";
            }
        }

        userRepository.save(user);  // ΟΛΑ σώζονται εδώ λόγω cascade
        return "redirect:/login";
    }


    private List<String> splitAndTrim(String input) {
        if (input == null || input.isBlank()) return List.of();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
