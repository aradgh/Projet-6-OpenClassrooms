package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AddRelationController {

    private final UserService userService;

    public AddRelationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/add-relation")
    public String showAddRelationPage() {
        return "add-relation";
    }

    @PostMapping("/add-relation")
    public String addRelation(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("email") String email,
                              Model model) {
        try {
            // Récupère l'utilisateur connecté (celui qui ajoute une relation)
            User currentUser = userService.getUserByEmail(userDetails.getUsername());
            // Récupère la relation à ajouter en recherchant par email
            User relation = userService.getUserByEmail(email);

            // Vérifie que l'utilisateur ne tente pas de s'ajouter lui-même
            if (currentUser.getId().equals(relation.getId())) {
                throw new IllegalArgumentException("Vous ne pouvez pas vous ajouter comme relation.");
            }

            // Ajoute la relation
            userService.addConnection(currentUser, relation);
            model.addAttribute("message", "Relation ajoutée avec succès !");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'ajout de la relation : " + e.getMessage());
        }
        return "add-relation";
    }
}
