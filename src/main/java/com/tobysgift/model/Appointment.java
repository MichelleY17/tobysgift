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

    public Appointment() {
    }
    
    /**
     * Costruttore con parametri principali
     * 
     * @param user utente che ha richiesto l'appuntamento
     * @param professional professionista con cui è stato richiesto l'appuntamento
     * @param dataOra data e ora dell'appuntamento
     */
    public Appointment(User user, Professional professional, LocalDateTime dataOra) {
        this.user = user;
        this.professional = professional;
        this.dataOra = dataOra;
    }
    
    /**
     * Costruttore completo
     * 
     * @param user utente che ha richiesto l'appuntamento
     * @param professional professionista con cui è stato richiesto l'appuntamento
     * @param dataOra  data e ora dell'appuntamento
     * @param note  note aggiuntive
     */
    public Appointment(User user, Professional professional, LocalDateTime dataOra, String note) {
        this.user = user;
        this.professional = professional;
        this.dataOra = dataOra;
        this.note = note;
    }
    

    
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
     * per verificare se l'appuntamneto  è già passato uso il metodo isPassato()
     * 
     * @return true =  la data dell'appuntamento è passata //false  = appuntamneto da svolgersi
     */
    public boolean isPassato() {
        return dataOra.isBefore(LocalDateTime.now());
    }
    
    /**
     *  per vedere se si puo annullare l'appuntamneto  si devono verificare queste condizioni:
     *  sono nello stato: RICHIESTO o CONFERMATO e non sono già passati
     * 
     * @return restituisce true = appuntamento  annullabile //false = lo stato dell'appuntamento è passato
     */
    public boolean isAnnullabile() {
        return (status == AppointmentStatus.RICHIESTO || status == AppointmentStatus.CONFERMATO) && !isPassato();
    }
    
    /**
     * conferma appuntamento
     * 
     * @return true = operazione è andata a buon fine // false =  non può essere confermato
     */
    public boolean conferma() {
        if (status == AppointmentStatus.RICHIESTO && !isPassato()) {
            status = AppointmentStatus.CONFERMATO;
            return true;
        }
        return false;
    }
    
    /**
     * Completa l'appuntamento
     * 
     * @return true = l'operazione è andata a buon fine // false = l'appuntamento non può essere completato
     */
    public boolean completa() {
        if (status == AppointmentStatus.CONFERMATO) {
            status = AppointmentStatus.COMPLETATO;
            return true;
        }
        return false;
    }
    
    /**
     * annulla appuntamento
     * 
     * @return true = l'operazione è andata a buon fine // false = l'appuntamento non può essere annullato
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
