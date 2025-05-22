package _5336_4701_5281.swdeproj.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

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

