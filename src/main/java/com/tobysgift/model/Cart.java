package com.tobysgift.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta il carrello di un utente.
 */
@Entity
@Table(name = "carts")
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private BigDecimal totale = BigDecimal.ZERO;
    
    /**
     * Costruttore di default.
     */
    public Cart() {
    }
    
    /**
     * Costruttore con utente.
     * 
     * @param user L'utente proprietario del carrello
     */
    public Cart(User user) {
        this.user = user;
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
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    public BigDecimal getTotale() {
        return totale;
    }
    
    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }
    
    /**
     * Aggiunge un prodotto al carrello.
     * 
     * @param product Il prodotto da aggiungere
     * @param quantita La quantità da aggiungere
     * @return L'elemento del carrello creato o aggiornato
     */
    public CartItem addProduct(Product product, int quantita) {
        // Cerca se il prodotto è già nel carrello
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                // Aggiorna la quantità
                item.setQuantita(item.getQuantita() + quantita);
                // Ricalcola il totale del carrello
                calcolaTotale();
                return item;
            }
        }
        
        // Se il prodotto non è nel carrello, crea un nuovo CartItem
        CartItem cartItem = new CartItem(this, product, quantita);
        items.add(cartItem);
        
        // Ricalcola il totale del carrello
        calcolaTotale();
        
        return cartItem;
    }
    
    /**
     * Rimuove un elemento dal carrello.
     * 
     * @param cartItem L'elemento da rimuovere
     */
    public void removeItem(CartItem cartItem) {
        items.remove(cartItem);
        
        // Ricalcola il totale del carrello
        calcolaTotale();
    }
    
    /**
     * Rimuove un prodotto dal carrello.
     * 
     * @param productId L'ID del prodotto da rimuovere
     * @return true se il prodotto è stato rimosso, false se non era presente
     */
    public boolean removeProduct(Long productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                items.remove(item);
                calcolaTotale();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Aggiorna la quantità di un prodotto nel carrello.
     * 
     * @param productId L'ID del prodotto da aggiornare
     * @param quantita La nuova quantità
     * @return true se l'aggiornamento è avvenuto, false se il prodotto non è nel carrello
     */
    public boolean updateQuantity(Long productId, int quantita) {
        if (quantita <= 0) {
            return removeProduct(productId);
        }
        
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantita(quantita);
                calcolaTotale();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calcola il totale del carrello in base agli elementi presenti.
     */
    public void calcolaTotale() {
        BigDecimal nuovoTotale = BigDecimal.ZERO;
        for (CartItem item : items) {
            nuovoTotale = nuovoTotale.add(item.getPrezzoTotale());
        }
        this.totale = nuovoTotale;
    }
    
    /**
     * Svuota il carrello.
     */
    public void svuota() {
        items.clear();
        totale = BigDecimal.ZERO;
    }
    
    /**
     * Conta il numero di elementi nel carrello.
     * 
     * @return Il numero di elementi
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Conta il numero totale di prodotti nel carrello (somma delle quantità).
     * 
     * @return Il numero totale di prodotti
     */
    public int getTotalProductCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantita();
        }
        return count;
    }
    
    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", items=" + items.size() +
                ", totale=" + totale +
                '}';
    }
}
