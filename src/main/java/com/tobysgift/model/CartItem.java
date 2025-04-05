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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cart_items")
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private int quantita = 1;
    
    @NotNull
    @Column(name = "prezzo_unitario", nullable = false)
    private BigDecimal prezzoUnitario;
    
    public CartItem() {
    }
    
    /**
     * Costruttore con parametri
     * 
     * @param cart carello del quale fa parte cartItem
     * @param product prodotto
     * @param quantita quantità
     */
    public CartItem(Cart cart, Product product, int quantita) {
        this.cart = cart;
        this.product = product;
        this.quantita = quantita;
        this.prezzoUnitario = product.getPrezzo();
    }
    

    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Cart getCart() {
        return cart;
    }
    
    public void setCart(Cart cart) {
        this.cart = cart;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.prezzoUnitario = product.getPrezzo();
        }
    }
    
    public int getQuantita() {
        return quantita;
    }
    
    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }
    
    public BigDecimal getPrezzoUnitario() {
        return prezzoUnitario;
    }
    
    public void setPrezzoUnitario(BigDecimal prezzoUnitario) {
        this.prezzoUnitario = prezzoUnitario;
    }
    
    /**
     * calcola il prezzo totale dell'elemento del carrello 
     * 
     * @return restitutisce = totale (prezzo unitario * quantità)
     */
    public BigDecimal getPrezzoTotale() {
        return prezzoUnitario.multiply(BigDecimal.valueOf(quantita));
    }
    
    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", product=" + (product != null ? product.getNome() : "null") +
                ", quantita=" + quantita +
                ", prezzoUnitario=" + prezzoUnitario +
                '}';
    }
}
