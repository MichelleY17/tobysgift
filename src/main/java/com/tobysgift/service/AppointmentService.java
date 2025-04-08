package com.tobysgift.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tobysgift.model.Appointment;
import com.tobysgift.model.AppointmentStatus;
import com.tobysgift.model.Professional;
import com.tobysgift.model.User;

public interface AppointmentService {
    
    /**
     * Per trovare un appuntamento per ID
     * 
     * @param id ID dell'appuntamento
     * @return Optional contenente l'appuntamento trovato o vuoto se non esiste
     */
    Optional<Appointment> findById(Long id);
    
    /**
     * Per trovare tutti gli appuntamenti di un utente
     * 
     * @param user Utente
     * @return Lista di appuntamenti
     */
    List<Appointment> findByUser(User user);
    
    /**
     * Per trovare gli appuntamenti di un utente (paginati)
     * 
     * @param user Utente
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByUser(User user, Pageable pageable);
    
    /**
     * Per trovare gli appuntamenti di un utente per stato
     * 
     * @param user Utente
     * @param status Stato degli appuntamenti
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByUserAndStatus(User user, AppointmentStatus status, Pageable pageable);
    
    /**
     * Per trovare gli appuntamenti di un professionista
     * 
     * @param professional Professionista
     * @return Lista di appuntamenti
     */
    List<Appointment> findByProfessional(Professional professional);
    
    /**
     * Per trovare gli appuntamenti di un professionista (paginati)
     * 
     * @param professional Professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByProfessional(Professional professional, Pageable pageable);
    
    /**
     * Per trovare gli appuntamenti futuri di un professionista
     * 
     * @param professional Professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findUpcomingByProfessional(Professional professional, Pageable pageable);
    
    /**
     * Per prenotare un nuovo appuntamento
     * 
     * @param user Utente che prenota
     * @param professionalId ID del professionista
     * @param dataOra Data e ora dell'appuntamento
     * @param note Note aggiuntive (opzionali)
     * @return L'appuntamento creato
     * @throws IllegalArgumentException se ci sono problemi con la prenotazione
     */
    Appointment bookAppointment(User user, Long professionalId, LocalDateTime dataOra, String note) throws IllegalArgumentException;
    
    /**
     * Per aggiornare un appuntamento esistente
     * 
     * @param appointmentId ID dell'appuntamento
     * @param dataOra Nuova data e ora
     * @param note Nuove note
     * @param currentUser Utente che sta effettuando la modifica
     * @return L'appuntamento aggiornato
     * @throws IllegalArgumentException se l'appuntamento non esiste o non può essere modificato
     */
    Appointment updateAppointment(Long appointmentId, LocalDateTime dataOra, String note, User currentUser) throws IllegalArgumentException;
    
    /**
     * Per annullare un appuntamento
     * 
     * @param appointmentId ID dell'appuntamento
     * @param currentUser Utente che sta effettuando l'annullamento
     * @return true se l'annullamento è avvenuto con successo, false altrimenti
     * @throws IllegalArgumentException se l'appuntamento non esiste o non può essere annullato
     */
    boolean cancelAppointment(Long appointmentId, User currentUser) throws IllegalArgumentException;
    
    /**
     * Per verificare se un professionista ha disponibilità in una determinata fascia oraria
     * 
     * @param professionalId ID del professionista
     * @param dataOra Data e ora da verificare
     * @return true se il professionista è disponibile, false altrimenti
     */
    boolean isProfessionalAvailable(Long professionalId, LocalDateTime dataOra);
    
    /**
     * Per ottenere gli appuntamenti in una determinata data
     * 
     * @param professionalId ID del professionista
     * @param date Data da controllare
     * @return Lista di appuntamenti
     */
    List<Appointment> getAppointmentsByDate(Long professionalId, LocalDateTime date);
    
    /**
     * Per trovare tutti gli appuntamenti (paginati)
     * 
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findAll(Pageable pageable);
    
    /**
     * Per trovare appuntamenti per stato
     * 
     * @param status Stato degli appuntamenti
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    
    /**
     * Per trovare appuntamenti di un professionista per stato
     * 
     * @param professional Professionista
     * @param status Stato degli appuntamenti
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByProfessionalAndStatus(Professional professional, AppointmentStatus status, Pageable pageable);
    
    /**
     * Per trovare appuntamenti per data
     * 
     * @param date Data degli appuntamenti
     * @return Lista di appuntamenti
     */
    List<Appointment> findAppointmentsByDate(LocalDate date);
    
    /**
     * Per trovare appuntamenti nei prossimi giorni
     * 
     * @param days Numero di giorni
     * @return Lista di appuntamenti
     */
    List<Appointment> findAppointmentsInNextDays(int days);
    
    /**
     * Per contare gli appuntamenti per stato
     * 
     * @param status Stato degli appuntamenti
     * @return Numero di appuntamenti
     */
    long countByStatus(AppointmentStatus status);
    
    /**
     * Per salvare un appuntamento
     * 
     * @param appointment Appuntamento da salvare
     * @return Appuntamento salvato
     */
    Appointment save(Appointment appointment);
}