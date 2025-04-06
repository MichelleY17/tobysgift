package com.tobysgift.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.User;
import com.tobysgift.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * per mostrare la pagina di login
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * permostrare la pagina di registrazione
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    /**
     * per gestire la registrazione di un nuovo utente
     */
    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") User user, 
                                BindingResult bindingResult, 
                                RedirectAttributes redirectAttributes) {
        
        // con questa condizione veridico errori di validazione
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        // qui si verifica se l'email è già in uso
        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email già in uso");
            return "register";
        }
        
        // con questa parte si verifica se l'username è già in uso 
        if (userService.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Nome utente già in uso");
            return "register";
        }
        
        try {
            // per registrare il nuovo utente
            userService.register(user);
            
            // per aggiungere messaggio di successo
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Registrazione completata con successo! Ora puoi accedere.");
            
            // per rendirizzare alla pagina di login
            return "redirect:/login";
        } catch (Exception e) {
            // gestione  eventuali errori
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Si è verificato un errore durante la registrazione: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    /**
     * per mostrare la pagina di recupero password
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
    
    /**
     * per gestire la richiesta di recupero password
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(String email, RedirectAttributes redirectAttributes) {
        // condizione che verifica se l'email esiste
        if (!userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Non è stato trovato alcun account con questa email");
            return "redirect:/forgot-password";
        }
        
        // essendo un progetto, il recupero password non averrà , ma mostrerà questo messaggio: "Se l'indirizzo email esiste nel nostro sistema, riceverai le istruzioni per reimpostare la password"
        
        redirectAttributes.addFlashAttribute("successMessage", 
                "Se l'indirizzo email esiste nel nostro sistema, riceverai le istruzioni per reimpostare la password");
        
        return "redirect:/login";
    }
}
