package com.tobysgift.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobysgift.model.Appointment;
import com.tobysgift.model.AppointmentStatus;
import com.tobysgift.model.Professional;
import com.tobysgift.model.User;
import com.tobysgift.repository.AppointmentRepository;
import com.tobysgift.repository.ProfessionalRepository;
import com.tobysgift.service.AppointmentService;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);
    
    // Durata standard di un appuntamento (in minuti)
    private static final int APPOINTMENT_DURATION_MINUTES = 60;
    
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    
    @Autowired
    public AppointmentServiceImpl(
        AppointmentRepository appointmentRepository,
        ProfessionalRepository professionalRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.professionalRepository = professionalRepository;
    }
    
    @Override
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }
    
    @Override
    public List<Appointment> findByUser(User user) {
        return appointmentRepository.findByUserOrderByDataOraDesc(user);
    }
    
    @Override
    public Page<Appointment> findByUser(User user, Pageable pageable) {
        return appointmentRepository.findByUser(user, pageable);
    }
    
    @Override
    public Page<Appointment> findByUserAndStatus(User user, AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByUserAndStatus(user, status, pageable);
    }
    
    @Override
    public List<Appointment> findByProfessional(Professional professional) {
        return appointmentRepository.findByProfessionalOrderByDataOra(professional);
    }
    
    @Override
    public Page<Appointment> findByProfessional(Professional professional, Pageable pageable) {
        return appointmentRepository.findByProfessional(professional, pageable);
    }
    
    @Override
    public Page<Appointment> findUpcomingByProfessional(Professional professional, Pageable pageable) {
        return appointmentRepository.findByProfessionalAndDataOraGreaterThanEqualOrderByDataOra(
            professional, LocalDateTime.now(), pageable);
    }
    
    @Override
    @Transactional
    public Appointment bookAppointment(User user, Long professionalId, LocalDateTime dataOra, String note) 
            throws IllegalArgumentException {
        
        // Validazione dei dati
        if (user == null) {
            throw new IllegalArgumentException("Utente non valido");
        }
        
        if (dataOra == null) {
            throw new IllegalArgumentException("Data e ora non valide");
        }
        
        if (dataOra.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Non è possibile prenotare appuntamenti nel passato");
        }
        
        // Trova il professionista
        Professional professional = professionalRepository.findById(professionalId)
            .orElseThrow(() -> new IllegalArgumentException("Professionista non trovato"));
        
        // Verifica la disponibilità
        if (!isProfessionalAvailable(professionalId, dataOra)) {
            throw new IllegalArgumentException("Il professionista non è disponibile in questo orario");
        }
        
        // Crea il nuovo appuntamento
        Appointment appointment = new Appointment(user, professional, dataOra);
        if (note != null && !note.trim().isEmpty()) {
            appointment.setNote(note);
        }
        
        // Imposta lo stato predefinito come RICHIESTO
        appointment.setStatus(AppointmentStatus.RICHIESTO);
        
        // Salva e restituisci l'appuntamento
        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Appuntamento prenotato: {}", savedAppointment);
        
        return savedAppointment;
    }
    
    @Override
    @Transactional
    public Appointment updateAppointment(Long appointmentId, LocalDateTime dataOra, String note, User currentUser) 
            throws IllegalArgumentException {
        
        // Trova l'appuntamento
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appuntamento non trovato"));
        
        // Verifica che l'utente corrente sia il proprietario dell'appuntamento
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a modificare questo appuntamento");
        }
        
        // Verifica che l'appuntamento sia modificabile
        if (appointment.getStatus() != AppointmentStatus.RICHIESTO && 
            appointment.getStatus() != AppointmentStatus.CONFERMATO) {
            throw new IllegalArgumentException("Non è possibile modificare un appuntamento " + 
                                              appointment.getStatus().getDescrizione().toLowerCase());
        }
        
        if (appointment.isPassato()) {
            throw new IllegalArgumentException("Non è possibile modificare un appuntamento passato");
        }
        
        // Se la data è cambiata, verifica la disponibilità
        if (dataOra != null && !dataOra.equals(appointment.getDataOra())) {
            if (dataOra.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Non è possibile spostare l'appuntamento nel passato");
            }
            
            if (!isProfessionalAvailable(appointment.getProfessional().getId(), dataOra)) {
                throw new IllegalArgumentException("Il professionista non è disponibile in questo orario");
            }
            
            // Aggiorna la data
            appointment.setDataOra(dataOra);
            
            // Quando si modifica la data, lo stato torna a RICHIESTO
            appointment.setStatus(AppointmentStatus.RICHIESTO);
        }
        
        // Aggiorna le note se necessario
        if (note != null) {
            appointment.setNote(note);
        }
        
        // Salva e restituisci l'appuntamento aggiornato
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        logger.info("Appuntamento aggiornato: {}", updatedAppointment);
        
        return updatedAppointment;
    }
    
    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId, User currentUser) throws IllegalArgumentException {
        // Trova l'appuntamento
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appuntamento non trovato"));
        
        // Verifica che l'utente corrente sia il proprietario dell'appuntamento
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato ad annullare questo appuntamento");
        }
        
        // Verifica che l'appuntamento sia annullabile
        if (!appointment.isAnnullabile()) {
            throw new IllegalArgumentException("Non è possibile annullare questo appuntamento");
        }
        
        // Annulla l'appuntamento
        boolean success = appointment.annulla();
        
        if (success) {
            appointmentRepository.save(appointment);
            logger.info("Appuntamento annullato: {}", appointment);
        }
        
        return success;
    }
    
    @Override
    public boolean isProfessionalAvailable(Long professionalId, LocalDateTime dataOra) {
        // Trova il professionista
        Professional professional = professionalRepository.findById(professionalId).orElse(null);
        if (professional == null) {
            return false;
        }
        
        // Calcola inizio e fine dell'appuntamento
        LocalDateTime startTime = dataOra;
        LocalDateTime endTime = dataOra.plusMinutes(APPOINTMENT_DURATION_MINUTES);
        
        // Verifica che non ci siano appuntamenti sovrapposti
        return !appointmentRepository.existsByProfessionalAndTimeSlot(professional, startTime, endTime);
    }
    
    @Override
    public List<Appointment> getAppointmentsByDate(Long professionalId, LocalDateTime date) {
        // Trova il professionista
        Professional professional = professionalRepository.findById(professionalId).orElse(null);
        if (professional == null) {
            return List.of();
        }
        
        // Calcola inizio e fine giorno
        LocalDateTime startOfDay = date.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        // Trova tutti gli appuntamenti del giorno
        return appointmentRepository.findByDataOraBetweenOrderByDataOra(startOfDay, endOfDay);
    }
    
    @Override
    public Page<Appointment> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }
    
    @Override
    public Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Page<Appointment> findByProfessionalAndStatus(Professional professional, AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByProfessionalAndStatus(professional, status, pageable);
    }
    
    @Override
    public List<Appointment> findAppointmentsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        return appointmentRepository.findByDataOraBetweenOrderByDataOra(startOfDay, endOfDay);
    }
    
    @Override
    public List<Appointment> findAppointmentsInNextDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);
        
        return appointmentRepository.findByDataOraBetweenOrderByDataOra(now, endDate);
    }
    
    @Override
    public long countByStatus(AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }
    
    @Override
    @Transactional
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
}