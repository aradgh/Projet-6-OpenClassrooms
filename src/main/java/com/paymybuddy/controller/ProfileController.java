package com.paymybuddy.controller;

import com.paymybuddy.model.User;
import com.paymybuddy.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Récupère l'utilisateur connecté grâce à son email
        User user = userService.getUserByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    // Affiche le formulaire d'édition de profil
    @GetMapping("/profile/edit")
    public String showEditProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile-edit";
    }

    // Traite la modification du profil
    @PostMapping("/profile/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String username,
                                @RequestParam String email,
                                @RequestParam(required = false) String password,
                                Model model) {
        try {
            // Récupérer l'utilisateur courant via l'ancien email
            User currentUser = userService.getUserByEmail(userDetails.getUsername());
            // Mettre à jour le profil
            userService.updateProfile(currentUser.getId(), username, email, password);

            // Mettre à jour le principal dans le SecurityContext
            UserDetails updatedUserDetails = userService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return "redirect:/profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // En cas d'erreur, rechargez le formulaire d'édition avec l'utilisateur actuel
            User updatedUser = userService.getUserByEmail(userDetails.getUsername());
            model.addAttribute("user", updatedUser);
            return "profile-edit";
        }
    }

}
