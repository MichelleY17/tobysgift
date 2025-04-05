package com.tobysgift.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Entità che rappresenta un prodotto in vendita.
 */
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome del prodotto è obbligatorio")
    private String nome;
    
    @Column(columnDefinition = "TEXT")
    private String descrizione;
    
    @NotNull(message = "Il prezzo è obbligatorio")
    @Positive(message = "Il prezzo deve essere maggiore di zero")
    private BigDecimal prezzo;
    
    @Min(value = 0, message = "La quantità disponibile non può essere negativa")
    @Column(name = "quantita_disponibile")
    private int quantitaDisponibile;
    
    private String immagine;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category categoria;
    
    /**
     * Costruttore di default.
     */
    public Product() {
    }
    
    /**
     * Costruttore con parametri principali.
     * 
     * @param nome Il nome del prodotto
     * @param descrizione La descrizione del prodotto
     * @param prezzo Il prezzo del prodotto
     * @param quantitaDisponibile La quantità disponibile
     * @param categoria La categoria del prodotto
     */
    public Product(String nome, String descrizione, BigDecimal prezzo, int quantitaDisponibile, Category categoria) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
        this.quantitaDisponibile = quantitaDisponibile;
        this.categoria = categoria;
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
    
    public BigDecimal getPrezzo() {
        return prezzo;
    }
    
    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }
    
    public int getQuantitaDisponibile() {
        return quantitaDisponibile;
    }
    
    public void setQuantitaDisponibile(int quantitaDisponibile) {
        this.quantitaDisponibile = quantitaDisponibile;
    }
    
    public String getImmagine() {
        return immagine;
    }
    
    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }
    
    public Category getCategoria() {
        return categoria;
    }
    
    public void setCategoria(Category categoria) {
        this.categoria = categoria;
    }
    
    /**
     * Verifica se il prodotto è disponibile.
     * 
     * @return true se la quantità disponibile è maggiore di 0, false altrimenti
     */
    public boolean isDisponibile() {
        return quantitaDisponibile > 0;
    }
    
    /**
     * Decrementa la quantità disponibile del prodotto.
     * 
     * @param quantita La quantità da decrementare
     * @return true se l'operazione è andata a buon fine, false se la quantità richiesta non è disponibile
     */
    public boolean decrementaQuantita(int quantita) {
        if (quantita <= 0 || quantita > quantitaDisponibile) {
            return false;
        }
        this.quantitaDisponibile -= quantita;
        return true;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", prezzo=" + prezzo +
                ", quantitaDisponibile=" + quantitaDisponibile +
                ", categoria=" + (categoria != null ? categoria.getNome() : "null") +
                '}';
    }
}
