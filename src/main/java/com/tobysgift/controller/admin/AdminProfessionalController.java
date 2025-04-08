package com.tobysgift.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Professional;
import com.tobysgift.model.ProfessionalType;
import com.tobysgift.service.ProfessionalService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/professionals")
public class AdminProfessionalController {
    
    private final ProfessionalService professionalService;
    
    @Autowired
    public AdminProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }
    
    /**
     * per mostrare la lista dei professionisti per l'amministratore
     */
    @GetMapping
    public String listProfessionals(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome,asc") String sort) {
        
        // Parsing del parametro di ordinamento
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
                                       Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Creazione dell'oggetto Pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        // Recupero dei professionisti in base ai parametri
        Page<Professional> professionalPage;
        
        if (search != null && !search.isEmpty() && type != null && !type.isEmpty()) {
            // Ricerca per parola chiave e tipo
            Optional<ProfessionalType> profType = professionalService.findProfessionalTypeByName(type);
            if (profType.isPresent()) {
                professionalPage = professionalService.searchProfessionalsByType(search, profType.get().getId(), pageable);
            } else {
                professionalPage = professionalService.searchProfessionals(search, pageable);
            }
        } else if (search != null && !search.isEmpty()) {
            // Ricerca per parola chiave
            professionalPage = professionalService.searchProfessionals(search, pageable);
        } else if (type != null && !type.isEmpty()) {
            // Filtro per tipo
            Optional<ProfessionalType> profType = professionalService.findProfessionalTypeByName(type);
            if (profType.isPresent()) {
                professionalPage = professionalService.findByType(profType.get().getId(), pageable);
            } else {
                professionalPage = professionalService.findAll(pageable);
            }
        } else {
            // Nessun filtro
            professionalPage = professionalService.findAll(pageable);
        }
        
        // Caricamento dei tipi di professionisti per i filtri
        List<ProfessionalType> professionalTypes = professionalService.findAllProfessionalTypes();
        
        // Preparazione del modello
        model.addAttribute("professionals", professionalPage);
        model.addAttribute("professionalTypes", professionalTypes);
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("sort", sort);
        
        return "admin/professionals";
    }
    
    /**
     * per mostrare il form per creare un nuovo professionista
     */
    @GetMapping("/new")
    public String newProfessionalForm(Model model) {
        model.addAttribute("professional", new Professional());
        model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
        model.addAttribute("mode", "create");
        
        return "admin/professional-form";
    }
    
    /**
     * per gestire il salvataggio di un nuovo professionista
     */
    @PostMapping("/new")
    public String createProfessional(
            @Valid @ModelAttribute("professional") Professional professional,
            BindingResult bindingResult,
            @RequestParam("typeId") Long typeId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
            model.addAttribute("mode", "create");
            return "admin/professional-form";
        }
        
        try {
            // Imposta il tipo di professionista
            Optional<ProfessionalType> type = professionalService.findProfessionalTypeById(typeId);
            if (type.isEmpty()) {
                bindingResult.rejectValue("tipo", "error.professional", "Tipo di professionista non valido");
                model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
                model.addAttribute("mode", "create");
                return "admin/professional-form";
            }
            
            professional.setTipo(type.get());
            
            // Salva il professionista
            professionalService.save(professional, imageFile);
            
            redirectAttributes.addFlashAttribute("successMessage", "Professionista creato con successo");
            return "redirect:/admin/professionals";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nel salvare il professionista: " + e.getMessage());
            model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
            model.addAttribute("mode", "create");
            return "admin/professional-form";
        }
    }
    
    /**
     * per mostrare il form per modificare un professionista esistente
     */
    @GetMapping("/{id}/edit")
    public String editProfessionalForm(@PathVariable("id") Long id, Model model) {
        Optional<Professional> professional = professionalService.findById(id);
        
        if (professional.isPresent()) {
            model.addAttribute("professional", professional.get());
            model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
            model.addAttribute("mode", "edit");
            
            return "admin/professional-form";
        } else {
            return "redirect:/admin/professionals";
        }
    }
    
    /**
     * per gestire l'aggiornamento di un professionista esistente
     */
    @PostMapping("/{id}/edit")
    public String updateProfessional(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("professional") Professional professional,
            BindingResult bindingResult,
            @RequestParam("typeId") Long typeId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!id.equals(professional.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID professionista non valido");
            return "redirect:/admin/professionals";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
            model.addAttribute("mode", "edit");
            return "admin/professional-form";
        }
        
        try {
            // Imposta il tipo di professionista
            Optional<ProfessionalType> type = professionalService.findProfessionalTypeById(typeId);
            if (type.isEmpty()) {
                bindingResult.rejectValue("tipo", "error.professional", "Tipo di professionista non valido");
                model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
                model.addAttribute("mode", "edit");
                return "admin/professional-form";
            }
            
            professional.setTipo(type.get());
            
            // Aggiorna il professionista
            professionalService.update(professional, imageFile);
            
            redirectAttributes.addFlashAttribute("successMessage", "Professionista aggiornato con successo");
            return "redirect:/admin/professionals";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nell'aggiornare il professionista: " + e.getMessage());
            model.addAttribute("professionalTypes", professionalService.findAllProfessionalTypes());
            model.addAttribute("mode", "edit");
            return "admin/professional-form";
        }
    }
    
    /**
     * per eliminare un professionista
     */
    @PostMapping("/delete")
    public String deleteProfessional(
            @RequestParam("professionalId") Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean deleted = professionalService.delete(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Professionista eliminato con successo");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Professionista non trovato");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nell'eliminare il professionista: " + e.getMessage());
        }
        
        return "redirect:/admin/professionals";
    }
}
