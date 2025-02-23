package com.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Retourne register.html
    }

    @PostMapping("/register")
    public String processRegistration() {
        // Logique pour enregistrer un utilisateur
        return "redirect:/login"; // Rediriger vers la page de connexion après inscription
    }
}
