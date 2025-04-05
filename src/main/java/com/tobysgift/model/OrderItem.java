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
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private int quantita = 1;
    
    @NotNull
    @Column(name = "prezzo_unitario", nullable = false)
    private BigDecimal prezzoUnitario;
    
    public OrderItem() {
    }
    
    /**
     * Costruttore semplice con parametri
     * 
     * @param order ordine a cui appartiene questo elemento
     * @param product prodotto
     * @param quantita  quantità
     */
    public OrderItem(Order order, Product product, int quantita) {
        this.order = order;
        this.product = product;
        this.quantita = quantita;
        this.prezzoUnitario = product.getPrezzo();
    }
    
    /**
     * Costruttore completo
     * 
     * @param order ordine a cui appartiene questo elemento
     * @param product prodotto
     * @param quantita quantità
     * @param prezzoUnitario Il prezzo unitario (potrebbe essere diverso dal prezzo attuale del prodotto)
     */
    public OrderItem(Order order, Product product, int quantita, BigDecimal prezzoUnitario) {
        this.order = order;
        this.product = product;
        this.quantita = quantita;
        this.prezzoUnitario = prezzoUnitario;
    }
    
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null && this.prezzoUnitario == null) {
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
     * Calcola totale dell'elemento dell'ordine
     * 
     * @return  prezzo totale (prezzo unitario * quantità)
     */
    public BigDecimal getPrezzoTotale() {
        return prezzoUnitario.multiply(BigDecimal.valueOf(quantita));
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", product=" + (product != null ? product.getNome() : "null") +
                ", quantita=" + quantita +
                ", prezzoUnitario=" + prezzoUnitario +
                '}';
    }
}
