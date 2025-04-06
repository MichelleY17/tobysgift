package com.tobysgift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.User;
import com.tobysgift.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        // Ottieni l'utente corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        // Ottieni l'utente corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        model.addAttribute("user", user);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("user") User user, 
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "profile-edit";
        }
        
        try {
            userService.updateProfile(user);
            redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante l'aggiornamento del profilo: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        
        // per verificare  che le nuove password corrispondano
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le nuove password non corrispondono");
            return "redirect:/profile/change-password";
        }
        
        // per ottenere l'utente corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        try {
            boolean success = userService.changePassword(email, oldPassword, newPassword);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Password modificata con successo!");
                return "redirect:/profile";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "La password attuale non è corretta");
                return "redirect:/profile/change-password";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante la modifica della password: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    @GetMapping("/change-username")
    public String changeUsernameForm(Model model) {
        // per ottenere l'utente corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        model.addAttribute("currentUsername", user.getUsername());
        return "change-username";
    }

    @PostMapping("/change-username")
    public String changeUsername(@RequestParam("newUsername") String newUsername,
                                RedirectAttributes redirectAttributes) {
        
        // per ottenere l'utente corrente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        try {
            boolean success = userService.changeUsername(email, newUsername);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Nome utente modificato con successo!");
                return "redirect:/profile";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Il nome utente è già in uso");
                return "redirect:/profile/change-username";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante la modifica del nome utente: " + e.getMessage());
            return "redirect:/profile/change-username";
        }
    }
}
