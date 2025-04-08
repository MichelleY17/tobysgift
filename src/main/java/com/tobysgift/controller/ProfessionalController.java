package com.tobysgift.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tobysgift.model.Professional;
import com.tobysgift.model.ProfessionalType;
import com.tobysgift.service.ProfessionalService;

@Controller
@RequestMapping("/professionals")
public class ProfessionalController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfessionalController.class);
    
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final String DEFAULT_SORT = "nome,asc";

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }
    
    /**
     * Gestisce la lista dei professionisti con paginazione e filtri
     */
    @GetMapping
    public String listProfessionals(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long typeId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
        @RequestParam(defaultValue = DEFAULT_SORT) String sort,
        Model model
    ) {
        // Validazione e normalizzazione dei parametri
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        
        // Log della richiesta
        logger.info("Richiesta lista professionisti - Pagina: {}, Dimensione: {}, Ricerca: {}, Tipo: {}", 
            page, size, search, typeId);

        // Gestione ordinamento
        Sort sortObj = validateAndCreateSort(sort);
        
        // Preparazione paginazione
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        // Ricerca dei professionisti
        Page<Professional> professionalsPage = findProfessionals(search, typeId, pageable);
        
        // Recupero tipi di professionisti per filtro
        List<ProfessionalType> professionalTypes = professionalService.findAllProfessionalTypes();
        
        // Preparazione modello
        model.addAttribute("professionals", professionalsPage.getContent());
        model.addAttribute("page", professionalsPage);
        model.addAttribute("professionalTypes", professionalTypes);
        model.addAttribute("search", search);
        model.addAttribute("typeId", typeId);
        model.addAttribute("sort", sort);
        model.addAttribute("professionalsCount", professionalService.countProfessionals());
        
        return "professionals/list";
    }

    /**
     * Metodo interno per validare e creare l'oggetto Sort
     */
    private Sort validateAndCreateSort(String sort) {
        try {
            String[] sortParams = sort.split(",");
            if (sortParams.length != 2) {
                logger.warn("Formato ordinamento non valido: {}", sort);
                return Sort.by(Sort.Direction.ASC, "nome");
            }
            
            Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            return Sort.by(direction, sortParams[0]);
        } catch (Exception e) {
            logger.error("Errore nella creazione dell'ordinamento", e);
            return Sort.by(Sort.Direction.ASC, "nome");
        }
    }

    /**
     * Metodo interno per trovare i professionisti con gestione dei filtri
     */
    private Page<Professional> findProfessionals(String search, Long typeId, Pageable pageable) {
        try {
            if (search != null && !search.isEmpty()) {
                // Ricerca con termine
                if (typeId != null) {
                    // Ricerca con tipo e termine
                    return professionalService.searchProfessionalsByType(search, typeId, pageable);
                } else {
                    // Ricerca solo per termine
                    return professionalService.searchProfessionals(search, pageable);
                }
            } else {
                // Ricerca per tipo se specificato
                if (typeId != null) {
                    return professionalService.findByType(typeId, pageable);
                } else {
                    // Tutti i professionisti
                    return professionalService.findAll(pageable);
                }
            }
        } catch (Exception e) {
            logger.error("Errore nella ricerca dei professionisti", e);
            return Page.empty(pageable);
        }
    }

    /**
     * Gestisce il dettaglio di un professionista
     */
    // @GetMapping("/{id}")
    // public String getProfessionalDetail(
    //     @PathVariable Long id, 
    //     Model model,
    //     Authentication authentication
    // ) {
    //     try {
    //         // Trova il professionista
    //         Optional<Professional> professional = professionalService.findById(id);
            
    //         if (professional.isPresent()) {
    //             // Log dell'accesso al dettaglio
    //             logger.info("Richiesto dettaglio professionista ID: {}", id);
                
    //             // Preparazione modello
    //             model.addAttribute("professional", professional.get());
                
    //             return "professionals/professionals-detail-template";
    //         } else {
    //             // Professionista non trovato
    //             logger.warn("Tentativo di accedere a professionista non esistente - ID: {}", id);
    //             model.addAttribute("errorMessage", "Professionista non trovato");
    //             return "error-page";
    //         }
    //     } catch (Exception e) {
    //         logger.error("Errore nel recupero del professionista", e);
    //         model.addAttribute("errorMessage", "Errore durante il recupero del professionista");
    //         return "error-page";
    //     }
    // }
    @GetMapping("/{id}")
    public String getProfessionalDetail(
    @PathVariable Long id, 
    Model model,
    Authentication authentication
    ) {
    // Validazione esplicita dell'ID
    if (id == null || id <= 0) {
        logger.warn("Tentativo di accedere a professionista con ID non valido");
        return "redirect:/professionals";
    }

    try {
        Optional<Professional> professional = professionalService.findById(id);
        
        if (professional.isPresent()) {
            model.addAttribute("professional", professional.get());
            return "professionals/detail";
        } else {
            logger.warn("Professionista non trovato con ID: {}", id);
            return "redirect:/professionals";
        }
    } catch (Exception e) {
        logger.error("Errore nel recupero del professionista", e);
        return "redirect:/professionals";
    }
    }
}
