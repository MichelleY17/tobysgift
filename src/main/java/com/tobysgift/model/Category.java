package com.tobysgift.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;


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
     * Costruttore  default
     */
    public Category() {
    }
    
    /**
     * Costruttore con i paramnetri
     * 
     * @param nome  nome categoria
     * @param descrizione descrizione categoria
     */
    public Category(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }
    
    
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
     * per aggiungere un prodotto a una categoria sicrea il metodo addProduct
     * 
     * @param product prodotto da aggiungere
     */
    public void addProduct(Product product) {
        prodotti.add(product);
        product.setCategoria(this);
    }
    
    /**
     * per rimuovere un prodotto dalla categoria si creal il metodo removeProduct()
     * 
     * @param product prodotto da rimuovere
     */
    public void removeProduct(Product product) {
        prodotti.remove(product);
        product.setCategoria(null);
    }
    /*
     * toString per stampare Category e convertirla en stringa, perche senza si ottiene l'indirizzo dell'oggetto
     */
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}