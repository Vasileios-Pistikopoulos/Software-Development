package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.model.Traineeship;
import _5336_4701_5281.swdeproj.model.User;
import _5336_4701_5281.swdeproj.repository.TraineeProfileRepository;
import _5336_4701_5281.swdeproj.repository.TraineeshipRepository;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final UserRepository userRepo;
    private final TraineeProfileRepository traineeRepo;
    private final TraineeshipRepository traineeshipRepo;

    public HomeController(UserRepository userRepo,
                         TraineeProfileRepository traineeRepo,
                         TraineeshipRepository traineeshipRepo) {
        this.userRepo = userRepo;
        this.traineeRepo = traineeRepo;
        this.traineeshipRepo = traineeshipRepo;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getName().equals("anonymousUser")) {
            User user = userRepo.findByUsername(auth.getName())
                    .orElse(null);
            
            if (user != null && user.getRole() == User.Role.ROLE_TRAINEE) {
                Traineeship activeTraineeship = traineeshipRepo.findFirstByAssignedTraineeIdOrderByStartDateDesc(
                    traineeRepo.findByUserId(user.getId()).getId());
                model.addAttribute("activeTraineeship", activeTraineeship);
            }
        }
    }

    @GetMapping("/")
    public String home() {
        logger.info("Accessing home page");
        return "home";
    }

    @GetMapping("/home")
    public String homePage() {
        logger.info("Accessing home page via /home");
        return "home";
    }
}

