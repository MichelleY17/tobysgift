package com.tobysgift.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/admin")
public class DashboardController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DashboardController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Visualizza la dashboard dell'amministratore
     */
    @GetMapping("/dashboard")
    public String viewDashboard(Model model, Authentication authentication) {
        // Ottiene l'utente corrente (admin) tramite email
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        
        model.addAttribute("user", user);
        return "admin/dashboard"; // Corretto per riflettere il percorso del template
    }
    
    /**
     * Aggiorna il profilo dell'amministratore
     */
    @PostMapping("/dashboard/update-profile")
    public String updateAdminProfile(@Valid @ModelAttribute("user") User user, 
                                    BindingResult bindingResult,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        
        // Ottiene l'utente corrente (admin)
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email);
        
        // Verifica errori di validazione
        if (bindingResult.hasErrors()) {
            return "admin/dashboard"; 
        }
        // Mantiene i valori che non devono essere modificati
        user.setId(currentUser.getId());
        user.setEmail(currentUser.getEmail()); // email è readonly
        user.setUsername(currentUser.getUsername()); // username gestito separatamente
        user.setPassword(currentUser.getPassword()); // password gestita separatamente
        user.setRoles(currentUser.getRoles());
        user.setCreatedAt(currentUser.getCreatedAt());
        user.setLastLogin(currentUser.getLastLogin());
        user.setActive(currentUser.isActive());
        
        // Aggiorna il profilo
        userService.updateProfile(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato con successo");
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Cambia la password dell'amministratore
     */
    @PostMapping("/dashboard/change-password")
    public String changeAdminPassword(@RequestParam("oldPassword") String oldPassword,
                                    @RequestParam("newPassword") String newPassword,
                                    @RequestParam("confirmPassword") String confirmPassword,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        
        // Verifica che la nuova password e la conferma coincidano
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Le password non coincidono");
            return "redirect:/admin/dashboard";
        }
        
        // Verifica che la nuova password rispetti i requisiti minimi
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "La password deve essere di almeno 6 caratteri");
            return "redirect:/admin/dashboard";
        }
        
        // Ottiene l'email dell'admin corrente
        String email = authentication.getName();
        
        // Cambia la password
        boolean success = userService.changePassword(email, oldPassword, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Password modificata con successo");
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "La password attuale non è corretta");
        }
        
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Cambia lo username dell'amministratore
     */
    @PostMapping("/dashboard/change-username")
    public String changeAdminUsername(@RequestParam("newUsername") String newUsername,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Aggiungi log per debug
            System.out.println("Tentativo di cambio username a: " + newUsername);
            
            // Ottieni l'email dell'admin corrente
            String email = authentication.getName();
            System.out.println("Email utente: " + email);
            
            User user = userService.findByEmail(email);
            if (user == null) {
                System.out.println("Utente non trovato");
                redirectAttributes.addFlashAttribute("errorMessage", "Utente non trovato");
                return "redirect:/admin/dashboard";
            }
            
            // Verifica che il nuovo username rispetti i requisiti minimi
            if (newUsername.length() < 3 || newUsername.length() > 50) {
                redirectAttributes.addFlashAttribute("usernameError", "Il nome utente deve essere compreso tra 3 e 50 caratteri");
                return "redirect:/admin/dashboard";
            }
            
            // Cambia lo username
            boolean success = userService.changeUsername(email, newUsername);
            System.out.println("Cambio username risultato: " + (success ? "successo" : "fallito"));
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Nome utente modificato con successo");
            } else {
                redirectAttributes.addFlashAttribute("usernameError", "Il nome utente è già in uso");
            }
        } catch (Exception e) {
            System.err.println("Errore durante il cambio username: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante il cambio username: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
}
