package com.tobysgift.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta un professionista.
 */
@Entity
@Table(name = "professionals")
public class Professional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;
    
    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;
    
    @Email(message = "Inserisci un indirizzo email valido")
    @Column(unique = true)
    private String email;
    
    private String telefono;
    
    private String specializzazione;
    
    @Column(columnDefinition = "TEXT")
    private String descrizione;
    
    private String immagine;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_id", nullable = false)
    private ProfessionalType tipo;
    
    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();
    
    /**
     * Costruttore di default.
     */
    public Professional() {
    }
    
    /**
     * Costruttore con parametri principali.
     * 
     * @param nome Il nome del professionista
     * @param cognome Il cognome del professionista
     * @param email L'email del professionista
     * @param tipo Il tipo di professionista
     */
    public Professional(String nome, String cognome, String email, ProfessionalType tipo) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.tipo = tipo;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCognome() {
        return cognome;
    }
    
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getSpecializzazione() {
        return specializzazione;
    }
    
    public void setSpecializzazione(String specializzazione) {
        this.specializzazione = specializzazione;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getImmagine() {
        return immagine;
    }
    
    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }
    
    public ProfessionalType getTipo() {
        return tipo;
    }
    
    public void setTipo(ProfessionalType tipo) {
        this.tipo = tipo;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
    
    /**
     * Ottiene il nome completo del professionista.
     * 
     * @return Nome e cognome concatenati
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
    
    /**
     * Aggiunge un appuntamento a questo professionista.
     * 
     * @param appointment L'appuntamento da aggiungere
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setProfessional(this);
    }
    
    /**
     * Rimuove un appuntamento da questo professionista.
     * 
     * @param appointment L'appuntamento da rimuovere
     */
    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setProfessional(null);
    }
    
    @Override
    public String toString() {
        return "Professional{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", email='" + email + '\'' +
                ", specializzazione='" + specializzazione + '\'' +
                ", tipo=" + (tipo != null ? tipo.getNome() : "null") +
                '}';
    }
}