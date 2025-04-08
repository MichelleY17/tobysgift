package com.tobysgift.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Appointment;
import com.tobysgift.model.AppointmentStatus;
import com.tobysgift.model.Professional;
import com.tobysgift.model.User;
import com.tobysgift.service.AppointmentService;
import com.tobysgift.service.ProfessionalService;
import com.tobysgift.service.UserService;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    
    private final AppointmentService appointmentService;
    private final ProfessionalService professionalService;
    private final UserService userService;
    
    @Autowired
    public AppointmentController(
        AppointmentService appointmentService,
        ProfessionalService professionalService,
        UserService userService
    ) {
        this.appointmentService = appointmentService;
        this.professionalService = professionalService;
        this.userService = userService;
    }
    
    /**
     * Lista degli appuntamenti dell'utente corrente (paginati)
     */
    @GetMapping
    public String listAppointments(
        Model model,
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status
    ) {
        try {
            // Recupera l'utente corrente
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Configurazione paginazione
            Pageable pageable = PageRequest.of(page, size, Sort.by("dataOra").descending());
            
            // Trova gli appuntamenti
            Page<Appointment> appointmentsPage;
            if (status != null && !status.isEmpty()) {
                try {
                    AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
                    appointmentsPage = appointmentService.findByUserAndStatus(currentUser, statusEnum, pageable);
                } catch (IllegalArgumentException e) {
                    appointmentsPage = appointmentService.findByUser(currentUser, pageable);
                }
            } else {
                appointmentsPage = appointmentService.findByUser(currentUser, pageable);
            }
            
            // Preparazione modello
            model.addAttribute("appointments", appointmentsPage.getContent());
            model.addAttribute("page", appointmentsPage);
            model.addAttribute("status", status);
            model.addAttribute("statuses", AppointmentStatus.values());
            
            return "appointments/list";
        } catch (Exception e) {
            logger.error("Errore nel recupero degli appuntamenti", e);
            model.addAttribute("errorMessage", "Si è verificato un errore nel recupero degli appuntamenti");
            return "error-page";
        }
    }
    
    /**
     * Dettaglio di un appuntamento specifico
     */
    @GetMapping("/{id}")
    public String getAppointmentDetail(
        @PathVariable Long id,
        Model model,
        Authentication authentication
    ) {
        try {
            // Recupera l'utente corrente
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Verifica che l'appuntamento appartenga all'utente corrente
                if (!appointment.getUser().getId().equals(currentUser.getId())) {
                    logger.warn("Tentativo di accesso non autorizzato all'appuntamento ID: {}", id);
                    return "redirect:/appointments";
                }
                
                // Preparazione modello
                model.addAttribute("appointment", appointment);
                
                return "appointments/detail";
            } else {
                logger.warn("Appuntamento non trovato con ID: {}", id);
                return "redirect:/appointments";
            }
        } catch (Exception e) {
            logger.error("Errore nel recupero dell'appuntamento", e);
            model.addAttribute("errorMessage", "Si è verificato un errore nel recupero dell'appuntamento");
            return "error-page";
        }
    }
    
    /**
     * Endpoint per prenotare un appuntamento
     */
    @PostMapping("/book")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bookAppointment(
        @RequestParam Long professionalId,
        @RequestParam String appointmentDate,
        @RequestParam String appointmentTime,
        @RequestParam(required = false) String notes,
        Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Recupera l'utente corrente
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Utente non autenticato");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Converti la data e l'ora
            LocalDateTime appointmentDateTime = parseDateTime(appointmentDate, appointmentTime);
            
            // Verifica che la data non sia nel passato
            if (appointmentDateTime.isBefore(LocalDateTime.now())) {
                response.put("success", false);
                response.put("message", "Non è possibile prenotare un appuntamento nel passato");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Prenota l'appuntamento
            Appointment appointment = appointmentService.bookAppointment(
                currentUser, professionalId, appointmentDateTime, notes
            );
            
            response.put("success", true);
            response.put("message", "Appuntamento prenotato con successo");
            response.put("appointmentId", appointment.getId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Formato data/ora non valido");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Errore nella prenotazione dell'appuntamento", e);
            response.put("success", false);
            response.put("message", "Si è verificato un errore durante la prenotazione dell'appuntamento");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Modulo di modifica appuntamento
     */
    @GetMapping("/{id}/edit")
    public String editAppointmentForm(
        @PathVariable Long id,
        Model model,
        Authentication authentication
    ) {
        try {
            // Recupera l'utente corrente
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Verifica che l'appuntamento appartenga all'utente corrente
                if (!appointment.getUser().getId().equals(currentUser.getId())) {
                    logger.warn("Tentativo di accesso non autorizzato all'appuntamento ID: {}", id);
                    return "redirect:/appointments";
                }
                
                // Verifica che l'appuntamento sia modificabile
                if (!appointment.isAnnullabile()) {
                    model.addAttribute("errorMessage", "Non è possibile modificare questo appuntamento");
                    return "redirect:/appointments/" + id;
                }
                
                // Preparazione modello
                model.addAttribute("appointment", appointment);
                
                return "appointments/edit";
            } else {
                logger.warn("Appuntamento non trovato con ID: {}", id);
                return "redirect:/appointments";
            }
        } catch (Exception e) {
            logger.error("Errore nel recupero dell'appuntamento da modificare", e);
            model.addAttribute("errorMessage", "Si è verificato un errore nel recupero dell'appuntamento");
            return "error-page";
        }
    }
    
    /**
     * Endpoint per aggiornare un appuntamento
     */
    @PostMapping("/{id}/update")
    public String updateAppointment(
        @PathVariable Long id,
        @RequestParam String appointmentDate,
        @RequestParam String appointmentTime,
        @RequestParam(required = false) String notes,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Recupera l'utente corrente
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Converti la data e l'ora
            LocalDateTime appointmentDateTime = parseDateTime(appointmentDate, appointmentTime);
            
            // Aggiorna l'appuntamento
            appointmentService.updateAppointment(id, appointmentDateTime, notes, currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Appuntamento aggiornato con successo");
            return "redirect:/appointments/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/appointments/" + id + "/edit";
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Formato data/ora non valido");
            return "redirect:/appointments/" + id + "/edit";
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento dell'appuntamento", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Si è verificato un errore durante l'aggiornamento dell'appuntamento");
            return "redirect:/appointments/" + id + "/edit";
        }
    }
    
    /**
     * Endpoint per annullare un appuntamento
     */
    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
        @PathVariable Long id,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Recupera l'utente corrente
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Annulla l'appuntamento
            boolean success = appointmentService.cancelAppointment(id, currentUser);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Appuntamento annullato con successo");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Non è stato possibile annullare l'appuntamento");
            }
            
            return "redirect:/appointments";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/appointments/" + id;
        } catch (Exception e) {
            logger.error("Errore nell'annullamento dell'appuntamento", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Si è verificato un errore durante l'annullamento dell'appuntamento");
            return "redirect:/appointments/" + id;
        }
    }
    
    /**
     * Endpoint per recuperare gli appuntamenti di un professionista
     */
    @GetMapping("/professional/{id}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getProfessionalAppointments(
        @PathVariable Long id,
        Authentication authentication
    ) {
        try {
            // Recupera l'utente corrente
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Trova il professionista
            Optional<Professional> professionalOpt = professionalService.findById(id);
            if (professionalOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Trova gli appuntamenti
            List<Appointment> appointments = appointmentService.findByProfessional(professionalOpt.get());
            
            // Prepara la risposta
            List<Map<String, Object>> result = appointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.ANNULLATO)
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("dataOra", a.getDataOra().toString());
                    map.put("status", a.getStatus().name());
                    return map;
                })
                .toList();
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Errore nel recupero degli appuntamenti del professionista", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Metodo di utilità per recuperare l'utente corrente
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        String username;
        
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        
        return userService.findByEmail(username);
    }
    
    /**
     * Metodo di utilità per convertire stringa di data e ora in LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateStr, String timeStr) throws DateTimeParseException {
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = LocalTime.parse(timeStr);
        return LocalDateTime.of(date, time);
    }
}