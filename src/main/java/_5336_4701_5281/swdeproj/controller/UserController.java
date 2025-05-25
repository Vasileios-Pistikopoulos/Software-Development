package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.dto.UserRegistrationDto;
import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.*;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
        logger.info("Accessing login page");
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.info("Accessing registration page - GET /register");
        model.addAttribute("userDto", new UserRegistrationDto());
        logger.info("Returning signup view");
        return "signup";
    }

    @PostMapping("/register")
    @Transactional
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegistrationDto dto, 
                             BindingResult result, 
                             Model model) {
        try {
            logger.info("Starting registration process - POST /register");
            logger.info("Received registration request for username: {}", dto.getUsername());
            logger.info("Role selected: {}", dto.getRole());
            
            if (result.hasErrors()) {
                logger.error("Validation errors during registration: {}", result.getAllErrors());
                return "signup";
            }

            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                logger.warn("Registration failed - Username already exists: {}", dto.getUsername());
                result.rejectValue("username", "error.username", "Username already exists");
                return "signup";
            }

            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                logger.warn("Registration failed - Email already exists: {}", dto.getEmail());
                result.rejectValue("email", "error.email", "Email already exists");
                return "signup";
            }

            logger.info("Creating new user with role: {}", dto.getRole());
            
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            
            // Add role to the user
            try {
                User.Role role = User.Role.valueOf(dto.getRole());
                user.setRole(role);  // This will also add to roles set
                logger.info("Added role {} to user", role);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid role provided: {}", dto.getRole());
                result.rejectValue("role", "error.role", "Invalid role selected");
                return "signup";
            }

            try {
                switch (user.getRole()) {
                    case ROLE_TRAINEE -> {
                        logger.info("Setting up trainee profile for user: {}", dto.getUsername());
                        if (dto.getTraineeFullName() == null || dto.getTraineeFullName().isBlank()) {
                            logger.warn("Registration failed - Full name missing for trainee");
                            result.rejectValue("traineeFullName", "error.traineeFullName", "Full name is required for trainees");
                            return "signup";
                        }
                        // Check for duplicate studentId
                        if (traineeRepo.findByStudentId(dto.getStudentId()) != null) {
                            logger.warn("Registration failed - Student ID already exists: {}", dto.getStudentId());
                            result.rejectValue("studentId", "error.studentId", "A trainee with this Student ID already exists");
                            return "signup";
                        }
                        user.setFullName(dto.getTraineeFullName());
                        
                        // Create TraineeProfile
                        TraineeProfile profile = new TraineeProfile();
                        profile.setFullName(dto.getTraineeFullName());
                        profile.setStudentId(dto.getStudentId());
                        profile.setSkills(new HashSet<>(splitAndTrim(dto.getTraineeSkills())));
                        profile.setInterests(new HashSet<>(splitAndTrim(dto.getTraineeInterests())));
                        profile.setPreferredLocation(dto.getPreferredLocation());

                        profile.setUser(user);
                        user.setTraineeProfile(profile);
                    }
                    case ROLE_COMPANY -> {
                        logger.info("Setting up company profile for user: {}", dto.getUsername());
                        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
                            logger.warn("Registration failed - Company name missing");
                            result.rejectValue("companyName", "error.companyName", "Company name is required");
                            return "signup";
                        }
                        user.setFullName(dto.getCompanyName());
                        
                        // Create Company entity
                        Company company = new Company();
                        company.setName(dto.getCompanyName());
                        company.setEmail(dto.getEmail());
                        company.setAddress(dto.getCompanyLocation()); // Using location as address
                        company.setPhone(dto.getCompanyPhone()); // Set phone from DTO
                        company.setDescription(""); // Default empty description
                        company.setUser(user);
                        user.setCompany(company);

                        // Create CompanyProfile
                        CompanyProfile profile = new CompanyProfile();
                        profile.setCompanyName(dto.getCompanyName());
                        profile.setLocation(dto.getCompanyLocation());
                        profile.setUser(user);
                        profile.setCompany(company);
                        user.setCompanyProfile(profile);
                    }
                    case ROLE_PROFESSOR -> {
                        logger.info("Setting up professor profile for user: {}", dto.getUsername());
                        if (dto.getProfessorFullName() == null || dto.getProfessorFullName().isBlank()) {
                            logger.warn("Registration failed - Full name missing for professor");
                            result.rejectValue("professorFullName", "error.professorFullName", "Full name is required for professors");
                            return "signup";
                        }
                        user.setFullName(dto.getProfessorFullName());
                        ProfessorProfile profile = new ProfessorProfile();
                        profile.setFullName(dto.getProfessorFullName());
                        
                        // Set interests if provided
                        if (dto.getProfessorInterests() != null && !dto.getProfessorInterests().isBlank()) {
                            Set<String> interests = new HashSet<>(splitAndTrim(dto.getProfessorInterests()));
                            profile.setInterests(interests);
                        }

                        profile.setUser(user);
                        user.setProfessorProfile(profile);
                    }
                    case ROLE_COMMITTEE -> {
                        logger.info("Setting up committee profile for user: {}", dto.getUsername());
                        user.setFullName("Committee Member");
                        CommitteeProfile profile = new CommitteeProfile();
                        profile.setUser(user);
                        user.setCommitteeProfile(profile);
                    }
                    default -> {
                        logger.error("Registration failed - Invalid role selected: {}", dto.getRole());
                        result.rejectValue("role", "error.role", "Invalid role selected");
                        return "signup";
                    }
                }

                logger.info("Attempting to save user to database: {}", dto.getUsername());
                userRepository.save(user);
                logger.info("User saved successfully to database: {}", dto.getUsername());
                return "redirect:/login?registered=true";
            } catch (Exception e) {
                logger.error("Error during profile setup: " + e.getMessage(), e);
                model.addAttribute("error", "Error during profile setup: " + e.getMessage());
                return "signup";
            }
        } catch (Exception e) {
            logger.error("Error during user registration for username: " + dto.getUsername(), e);
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/test-register")
    @Transactional
    public String testRegister() {
        try {
            logger.info("Testing basic user registration");
            User user = new User("tester", "tester@mail.com", passwordEncoder.encode("123456"), "ROLE_TRAINEE");
            user.setFullName("Tester Trainee");
            userRepository.save(user);
            logger.info("Test user saved successfully");
            return "redirect:/login?test=true";
        } catch (Exception e) {
            logger.error("Test registration failed: " + e.getMessage(), e);
            return "redirect:/login?error=test-failed";
        }
    }

    private List<String> splitAndTrim(String input) {
        if (input == null || input.isBlank()) return List.of();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
