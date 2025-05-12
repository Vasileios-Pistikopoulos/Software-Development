package _5336_4701_5281.swdeproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @RequestMapping(value = {"","/home","/"})
    public String displayHomePage() {
        return "home"; // Assuming you have a home.html template in your resources/templates directory
    }
}
