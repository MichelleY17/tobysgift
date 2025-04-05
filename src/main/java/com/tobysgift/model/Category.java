package com.tobysgift.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta una categoria di prodotti.
 */
@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome della categoria è obbligatorio")
    @Column(unique = true)
    private String nome;
    
    private String descrizione;
    
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> prodotti = new ArrayList<>();
    
    /**
     * Costruttore di default.
     */
    public Category() {
    }
    
    /**
     * Costruttore con parametri.
     * 
     * @param nome Il nome della categoria
     * @param descrizione La descrizione della categoria
     */
    public Category(String nome, String descrizione) {
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
    
    public List<Product> getProdotti() {
        return prodotti;
    }
    
    public void setProdotti(List<Product> prodotti) {
        this.prodotti = prodotti;
    }
    
    /**
     * Aggiunge un prodotto alla categoria.
     * 
     * @param product Il prodotto da aggiungere
     */
    public void addProduct(Product product) {
        prodotti.add(product);
        product.setCategoria(this);
    }
    
    /**
     * Rimuove un prodotto dalla categoria.
     * 
     * @param product Il prodotto da rimuovere
     */
    public void removeProduct(Product product) {
        prodotti.remove(product);
        product.setCategoria(null);
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}