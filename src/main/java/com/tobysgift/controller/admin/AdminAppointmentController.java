package com.tobysgift.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Appointment;
import com.tobysgift.model.AppointmentStatus;
import com.tobysgift.model.Professional;
import com.tobysgift.service.AppointmentService;
import com.tobysgift.service.ProfessionalService;

@Controller
@RequestMapping("/admin/appointments")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAppointmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAppointmentController.class);
    private static final int DEFAULT_PAGE_SIZE = 15;
    
    private final AppointmentService appointmentService;
    private final ProfessionalService professionalService;
    
    @Autowired
    public AdminAppointmentController(
        AppointmentService appointmentService,
        ProfessionalService professionalService
    ) {
        this.appointmentService = appointmentService;
        this.professionalService = professionalService;
    }
    
    /**
     * Lista di tutti gli appuntamenti (paginati)
     */
    @GetMapping
    public String listAppointments(
        Model model,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long professionalId,
        @RequestParam(required = false) String date,
        @RequestParam(defaultValue = "dataOra,desc") String sort
    ) {
        try {
            // Validazione parametri
            page = Math.max(0, page);
            size = Math.min(Math.max(1, size), 100);
            
            // Parsing del parametro di ordinamento
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
                                          Sort.Direction.DESC : Sort.Direction.ASC;
            
            // Creazione dell'oggetto Pageable
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            
            // Filtraggio appuntamenti
            Page<Appointment> appointmentsPage;
            if (status != null && !status.isEmpty()) {
                try {
                    AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
                    
                    if (professionalId != null) {
                        // Filtra per stato e professionista
                        Optional<Professional> professional = professionalService.findById(professionalId);
                        if (professional.isPresent()) {
                            appointmentsPage = appointmentService.findByProfessionalAndStatus(
                                professional.get(), statusEnum, pageable);
                        } else {
                            appointmentsPage = appointmentService.findByStatus(statusEnum, pageable);
                        }
                    } else {
                        // Filtra solo per stato
                        appointmentsPage = appointmentService.findByStatus(statusEnum, pageable);
                    }
                } catch (IllegalArgumentException e) {
                    if (professionalId != null) {
                        // Filtra solo per professionista
                        Optional<Professional> professional = professionalService.findById(professionalId);
                        if (professional.isPresent()) {
                            appointmentsPage = appointmentService.findByProfessional(professional.get(), pageable);
                        } else {
                            appointmentsPage = appointmentService.findAll(pageable);
                        }
                    } else {
                        // Nessun filtro
                        appointmentsPage = appointmentService.findAll(pageable);
                    }
                }
            } else if (professionalId != null) {
                // Filtra solo per professionista
                Optional<Professional> professional = professionalService.findById(professionalId);
                if (professional.isPresent()) {
                    appointmentsPage = appointmentService.findByProfessional(professional.get(), pageable);
                } else {
                    appointmentsPage = appointmentService.findAll(pageable);
                }
            } else {
                // Nessun filtro
                appointmentsPage = appointmentService.findAll(pageable);
            }
            
            // Recupera la lista di professionisti per il filtro
            List<Professional> professionals = professionalService.findAll();
            
            // Preparazione modello
            model.addAttribute("appointments", appointmentsPage.getContent());
            model.addAttribute("page", appointmentsPage);
            model.addAttribute("status", status);
            model.addAttribute("statuses", AppointmentStatus.values());
            model.addAttribute("professionals", professionals);
            model.addAttribute("selectedProfessionalId", professionalId);
            model.addAttribute("date", date);
            model.addAttribute("sort", sort);
            
            return "admin/appointments";
        } catch (Exception e) {
            logger.error("Errore nel recupero degli appuntamenti", e);
            model.addAttribute("errorMessage", "Si è verificato un errore nel recupero degli appuntamenti");
            return "admin/error";
        }
    }
    
    /**
     * Dettaglio di un appuntamento specifico
     */
    @GetMapping("/{id}")
    public String getAppointmentDetail(
        @PathVariable Long id,
        Model model
    ) {
        try {
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Preparazione modello
                model.addAttribute("appointment", appointment);
                model.addAttribute("statuses", AppointmentStatus.values());
                
                return "admin/appointment-detail";
            } else {
                logger.warn("Appuntamento non trovato con ID: {}", id);
                return "redirect:/admin/appointments";
            }
        } catch (Exception e) {
            logger.error("Errore nel recupero dell'appuntamento", e);
            model.addAttribute("errorMessage", "Si è verificato un errore nel recupero dell'appuntamento");
            return "admin/error";
        }
    }
    
    /**
     * Endpoint per confermare un appuntamento
     */
    @PostMapping("/{id}/confirm")
    public String confirmAppointment(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Tenta di confermare l'appuntamento
                boolean success = appointment.conferma();
                
                if (success) {
                    // Salva l'appuntamento aggiornato
                    appointmentService.save(appointment);
                    redirectAttributes.addFlashAttribute("successMessage", "Appuntamento confermato con successo");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Non è possibile confermare questo appuntamento (solo gli appuntamenti richiesti non passati possono essere confermati)");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Appuntamento non trovato");
            }
            
            return "redirect:/admin/appointments/" + id;
        } catch (Exception e) {
            logger.error("Errore nella conferma dell'appuntamento", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Si è verificato un errore durante la conferma dell'appuntamento");
            return "redirect:/admin/appointments/" + id;
        }
    }
    
    /**
     * Endpoint per completare un appuntamento
     */
    @PostMapping("/{id}/complete")
    public String completeAppointment(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Tenta di completare l'appuntamento
                boolean success = appointment.completa();
                
                if (success) {
                    // Salva l'appuntamento aggiornato
                    appointmentService.save(appointment);
                    redirectAttributes.addFlashAttribute("successMessage", "Appuntamento completato con successo");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Non è possibile completare questo appuntamento (solo gli appuntamenti confermati possono essere completati)");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Appuntamento non trovato");
            }
            
            return "redirect:/admin/appointments/" + id;
        } catch (Exception e) {
            logger.error("Errore nel completamento dell'appuntamento", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Si è verificato un errore durante il completamento dell'appuntamento");
            return "redirect:/admin/appointments/" + id;
        }
    }
    
    /**
     * Endpoint per annullare un appuntamento
     */
    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Tenta di annullare l'appuntamento
                boolean success = appointment.annulla();
                
                if (success) {
                    // Salva l'appuntamento aggiornato
                    appointmentService.save(appointment);
                    redirectAttributes.addFlashAttribute("successMessage", "Appuntamento annullato con successo");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Non è possibile annullare questo appuntamento (solo gli appuntamenti richiesti o confermati non passati possono essere annullati)");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Appuntamento non trovato");
            }
            
            return "redirect:/admin/appointments/" + id;
        } catch (Exception e) {
            logger.error("Errore nell'annullamento dell'appuntamento", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Si è verificato un errore durante l'annullamento dell'appuntamento");
            return "redirect:/admin/appointments/" + id;
        }
    }
    
    /**
     * Aggiorna lo stato di un appuntamento
     */
    @PostMapping("/{id}/update-status")
    public String updateAppointmentStatus(
        @PathVariable Long id,
        @RequestParam("status") String statusStr,
        RedirectAttributes redirectAttributes
    ) {
        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(statusStr);
            
            // Trova l'appuntamento
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                
                // Verifica se lo stato è cambiato
                if (appointment.getStatus() != newStatus) {
                    boolean success = false;
                    
                    // Applica la transizione di stato appropriata
                    switch (newStatus) {
                        case CONFERMATO:
                            success = appointment.conferma();
                            break;
                        case COMPLETATO:
                            success = appointment.completa();
                            break;
                        case ANNULLATO:
                            success = appointment.annulla();
                            break;
                        default:
                            redirectAttributes.addFlashAttribute("errorMessage", 
                                "Transizione di stato non supportata");
                            return "redirect:/admin/appointments/" + id;
                    }
                    
                    if (success) {
                        appointmentService.save(appointment);
                        redirectAttributes.addFlashAttribute("successMessage", 
                            "Stato dell'appuntamento aggiornato a: " + newStatus.getDescrizione());
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Impossibile aggiornare lo stato dell'appuntamento a " + newStatus.getDescrizione());
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Appuntamento non trovato");
            }
            
            return "redirect:/admin/appointments/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Stato dell'appuntamento non valido: " + statusStr);
            return "redirect:/admin/appointments/" + id;
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento dello stato dell'appuntamento", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Si è verificato un errore durante l'aggiornamento dello stato dell'appuntamento: " + e.getMessage());
            return "redirect:/admin/appointments/" + id;
        }
    }
    
    /**
     * Dashboard appuntamenti
     */
    @GetMapping("/dashboard")
    public String appointmentsDashboard(Model model) {
        try {
            // Conta appuntamenti per stato
            long requestedCount = appointmentService.countByStatus(AppointmentStatus.RICHIESTO);
            long confirmedCount = appointmentService.countByStatus(AppointmentStatus.CONFERMATO);
            long completedCount = appointmentService.countByStatus(AppointmentStatus.COMPLETATO);
            long cancelledCount = appointmentService.countByStatus(AppointmentStatus.ANNULLATO);
            long totalCount = requestedCount + confirmedCount + completedCount + cancelledCount;
            
            // Appuntamenti oggi
            LocalDate today = LocalDate.now();
            List<Appointment> todayAppointments = appointmentService.findAppointmentsByDate(today);
            
            // Appuntamenti questa settimana
            List<Appointment> weekAppointments = appointmentService.findAppointmentsInNextDays(7);
            
            // Recupera professionisti più prenotati
            List<Professional> topProfessionals = professionalService.findMostBookedProfessionals(5);
            
            // Preparazione modello
            model.addAttribute("requestedCount", requestedCount);
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("cancelledCount", cancelledCount);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("todayAppointments", todayAppointments);
            model.addAttribute("weekAppointments", weekAppointments);
            model.addAttribute("topProfessionals", topProfessionals);
            
            return "admin/appointments-dashboard";
        } catch (Exception e) {
            logger.error("Errore nel recupero dei dati per la dashboard", e);
            model.addAttribute("errorMessage", "Si è verificato un errore nel recupero dei dati");
            return "admin/error";
        }
    }
}