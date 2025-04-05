package com.tobysgift.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta un tipo di professionista.
 */
@Entity
@Table(name = "professional_types")
public class ProfessionalType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome del tipo è obbligatorio")
    @Column(unique = true)
    private String nome;
    
    private String descrizione;
    
    @OneToMany(mappedBy = "tipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Professional> professionals = new ArrayList<>();
    
    /**
     * Costruttore di default.
     */
    public ProfessionalType() {
    }
    
    /**
     * Costruttore con parametri.
     * 
     * @param nome Il nome del tipo di professionista
     * @param descrizione La descrizione del tipo
     */
    public ProfessionalType(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
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
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public List<Professional> getProfessionals() {
        return professionals;
    }
    
    public void setProfessionals(List<Professional> professionals) {
        this.professionals = professionals;
    }
    
    /**
     * Aggiunge un professionista a questo tipo.
     * 
     * @param professional Il professionista da aggiungere
     */
    public void addProfessional(Professional professional) {
        professionals.add(professional);
        professional.setTipo(this);
    }
    
    /**
     * Rimuove un professionista da questo tipo.
     * 
     * @param professional Il professionista da rimuovere
     */
    public void removeProfessional(Professional professional) {
        professionals.remove(professional);
        professional.setTipo(null);
    }
    
    @Override
    public String toString() {
        return "ProfessionalType{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}