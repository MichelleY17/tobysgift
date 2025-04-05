package com.tobysgift.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

/**
 * Entità che rappresenta un appuntamento con un professionista.
 */
@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;
    
    @NotNull(message = "La data e ora dell'appuntamento sono obbligatorie")
    @FutureOrPresent(message = "La data e ora dell'appuntamento devono essere future o presenti")
    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.RICHIESTO;
    
    /**
     * Costruttore di default.
     */
    public Appointment() {
    }
    
    /**
     * Costruttore con parametri principali.
     * 
     * @param user L'utente che ha richiesto l'appuntamento
     * @param professional Il professionista con cui è stato richiesto l'appuntamento
     * @param dataOra La data e ora dell'appuntamento
     */
    public Appointment(User user, Professional professional, LocalDateTime dataOra) {
        this.user = user;
        this.professional = professional;
        this.dataOra = dataOra;
    }
    
    /**
     * Costruttore completo.
     * 
     * @param user L'utente che ha richiesto l'appuntamento
     * @param professional Il professionista con cui è stato richiesto l'appuntamento
     * @param dataOra La data e ora dell'appuntamento
     * @param note Le note aggiuntive
     */
    public Appointment(User user, Professional professional, LocalDateTime dataOra, String note) {
        this.user = user;
        this.professional = professional;
        this.dataOra = dataOra;
        this.note = note;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Professional getProfessional() {
        return professional;
    }
    
    public void setProfessional(Professional professional) {
        this.professional = professional;
    }
    
    public LocalDateTime getDataOra() {
        return dataOra;
    }
    
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    /**
     * Verifica se l'appuntamento è già passato.
     * 
     * @return true se la data dell'appuntamento è passata, false altrimenti
     */
    public boolean isPassato() {
        return dataOra.isBefore(LocalDateTime.now());
    }
    
    /**
     * Verifica se l'appuntamento è annullabile.
     * Gli appuntamenti sono annullabili solo se sono nello stato RICHIESTO o CONFERMATO e non sono già passati.
     * 
     * @return true se l'appuntamento è annullabile, false altrimenti
     */
    public boolean isAnnullabile() {
        return (status == AppointmentStatus.RICHIESTO || status == AppointmentStatus.CONFERMATO) && !isPassato();
    }
    
    /**
     * Conferma l'appuntamento.
     * 
     * @return true se l'operazione è andata a buon fine, false se l'appuntamento non può essere confermato
     */
    public boolean conferma() {
        if (status == AppointmentStatus.RICHIESTO && !isPassato()) {
            status = AppointmentStatus.CONFERMATO;
            return true;
        }
        return false;
    }
    
    /**
     * Completa l'appuntamento.
     * 
     * @return true se l'operazione è andata a buon fine, false se l'appuntamento non può essere completato
     */
    public boolean completa() {
        if (status == AppointmentStatus.CONFERMATO) {
            status = AppointmentStatus.COMPLETATO;
            return true;
        }
        return false;
    }
    
    /**
     * Annulla l'appuntamento.
     * 
     * @return true se l'operazione è andata a buon fine, false se l'appuntamento non può essere annullato
     */
    public boolean annulla() {
        if (isAnnullabile()) {
            status = AppointmentStatus.ANNULLATO;
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", professional=" + (professional != null ? professional.getNomeCompleto() : "null") +
                ", dataOra=" + dataOra +
                ", status=" + status +
                '}';
    }
}
