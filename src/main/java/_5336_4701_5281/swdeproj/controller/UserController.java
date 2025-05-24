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
import java.util.List;

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
                StringBuilder errorDetails = new StringBuilder();
                result.getAllErrors().forEach(error -> errorDetails.append(error.getDefaultMessage()).append("\n"));
                model.addAttribute("errorMessage", "Validation failed. Please check your input.");
                model.addAttribute("errorDetails", errorDetails.toString());
                return "error/registration-error";
            }

            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                logger.warn("Registration failed - Username already exists: {}", dto.getUsername());
                model.addAttribute("errorMessage", "Username already exists");
                model.addAttribute("errorDetails", "The username '" + dto.getUsername() + "' is already taken. Please choose a different username.");
                return "error/registration-error";
            }

            logger.info("Creating new user with role: {}", dto.getRole());
            logger.info("Incoming role string: {}", dto.getRole());
            
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.addRole(User.Role.valueOf(dto.getRole()));

            try {
                switch (dto.getRole()) {
                    case "ROLE_TRAINEE" -> {
                        logger.info("Setting up trainee profile for user: {}", dto.getUsername());
                        if (dto.getTraineeFullName() == null || dto.getTraineeFullName().isBlank()) {
                            logger.warn("Registration failed - Full name missing for trainee");
                            model.addAttribute("errorMessage", "Full name is required for trainees");
                            model.addAttribute("errorDetails", "The full name field cannot be empty for trainee registration.");
                            return "error/registration-error";
                        }
                        user.setFullName(dto.getTraineeFullName());
                        TraineeProfile profile = new TraineeProfile();
                        profile.setFullName(dto.getTraineeFullName());
                        profile.setStudentId(dto.getStudentId());
                        profile.setSkills(splitAndTrim(dto.getTraineeSkills()));
                        profile.setInterests(splitAndTrim(dto.getTraineeInterests()));
                        profile.setPreferredLocation(dto.getPreferredLocation());

                        profile.setUser(user);
                        user.setTraineeProfile(profile);
                    }
                    case "ROLE_COMPANY" -> {
                        logger.info("Setting up company profile for user: {}", dto.getUsername());
                        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
                            logger.warn("Registration failed - Company name missing");
                            model.addAttribute("errorMessage", "Company name is required");
                            model.addAttribute("errorDetails", "The company name field cannot be empty for company registration.");
                            return "error/registration-error";
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
                    case "ROLE_PROFESSOR" -> {
                        logger.info("Setting up professor profile for user: {}", dto.getUsername());
                        if (dto.getProfessorFullName() == null || dto.getProfessorFullName().isBlank()) {
                            logger.warn("Registration failed - Full name missing for professor");
                            model.addAttribute("errorMessage", "Full name is required for professors");
                            model.addAttribute("errorDetails", "The full name field cannot be empty for professor registration.");
                            return "error/registration-error";
                        }
                        user.setFullName(dto.getProfessorFullName());
                        ProfessorProfile profile = new ProfessorProfile();
                        profile.setFullName(dto.getProfessorFullName());
                        profile.setInterests(splitAndTrim(dto.getProfessorInterests()));

                        profile.setUser(user);
                        user.setProfessorProfile(profile);
                    }
                    case "ROLE_COMMITTEE" -> {
                        logger.info("Setting up committee profile for user: {}", dto.getUsername());
                        user.setFullName("Committee Member");
                        CommitteeProfile profile = new CommitteeProfile();
                        profile.setUser(user);
                        user.setCommitteeProfile(profile);
                    }
                    default -> {
                        logger.error("Registration failed - Invalid role selected: {}", dto.getRole());
                        model.addAttribute("errorMessage", "Invalid role selected");
                        model.addAttribute("errorDetails", "The selected role '" + dto.getRole() + "' is not valid.");
                        return "error/registration-error";
                    }
                }
            } catch (Exception e) {
                logger.error("Error during profile setup: " + e.getMessage(), e);
                model.addAttribute("errorMessage", "Error during profile setup");
                model.addAttribute("errorDetails", "An error occurred while setting up the user profile: " + e.getMessage() + "\n" + e.toString());
                return "error/registration-error";
            }

            logger.info("Attempting to save user to database: {}", dto.getUsername());
            try {
                userRepository.save(user);
                logger.info("User saved successfully to database: {}", dto.getUsername());
            } catch (Exception e) {
                logger.error("Failed to save user to database: " + e.getMessage(), e);
                model.addAttribute("errorMessage", "Could not save user to database");
                model.addAttribute("errorDetails", "Database error: " + e.getMessage() + "\n" + e.toString());
                return "error/registration-error";
            }

            logger.info("User registration completed successfully for: {}", dto.getUsername());
            return "redirect:/login?registered=true";
            
        } catch (Exception e) {
            logger.error("Error during user registration for username: " + dto.getUsername(), e);
            model.addAttribute("errorMessage", "An unexpected error occurred during registration");
            model.addAttribute("errorDetails", "Unexpected error: " + e.getMessage() + "\n" + e.toString());
            return "error/registration-error";
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
