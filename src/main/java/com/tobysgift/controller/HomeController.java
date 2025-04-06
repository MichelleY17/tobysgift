package com.tobysgift.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller per la gestione della home page.
 */
@Controller
public class HomeController {
    
    /**
     * per visualizzare la home 
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Qui aggiungeremo la logica per recuperare i prodotti in evidenza, categorie e post recenti
        return "home";
    }
}