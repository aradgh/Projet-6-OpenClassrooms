package com.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Bienvenue dans PayMyBuddy");
        return "index"; // correspond au template src/main/resources/templates/index.html
    }
}
