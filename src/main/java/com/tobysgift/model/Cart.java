package com.tobysgift.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    public Cart() {
    }
    
    /**
     * construttore con parametro utente perche l'utente è proprietario del carello
     * 
     * @param user utente proprietario del carrello
     */
    public Cart(User user) {
        this.user = user;
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
     * metodo per aggiungere un prodotto al carello e quantita
     * 
     * @param product prodotto da aggiungere
     * @param quantitan quantita da aggiungere
     * @return restituire  carrello creato o aggiornato
     */
    public CartItem addProduct(Product product, int quantita) {
        // il prodotto è nel carello?
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                // aggiorna la quantità
                item.setQuantita(item.getQuantita() + quantita);
                // Ricalcola totale carrello
                calcolaTotale();
                return item;
            }
        }
        
        // se non è nel carrello... crea un nuovo CartItem
        CartItem cartItem = new CartItem(this, product, quantita);
        items.add(cartItem);
        
        // Ricalcola totale del carrello
        calcolaTotale();
        
        return cartItem;
    }
    
    /**
     * Rimuove elemento dal carrello
     * 
     * @param cartItem elemento da rimuovere
     */
    public void removeItem(CartItem cartItem) {
        items.remove(cartItem);
        
        // Ricalcola totale  carrello
        calcolaTotale();
    }
    
    /**
     * Rimuove un prodotto dal carrello, serve:
     * 
     * @param productId ID del prodotto da rimuovere
     * @return  true  = prodotto è stato rimosso //  false = non era presente
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
     * aggiorna quantità di un prodotto nel carrello metodo updateQuantity()
     * 
     * @param productId ID del prodotto da aggiornare
     * @param quantita true = nuova quantità // false = prodotto non è nel carrello
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
     * Calcola  totale del carrello in base agli elementi presenti
     */
    public void calcolaTotale() {
        BigDecimal nuovoTotale = BigDecimal.ZERO;
        for (CartItem item : items) {
            nuovoTotale = nuovoTotale.add(item.getPrezzoTotale());
        }
        this.totale = nuovoTotale;
    }
    
    /**
     * svuota carrello
     */
    public void svuota() {
        items.clear();
        totale = BigDecimal.ZERO;
    }
    
    /**
     * Conta numero  elementi nel carrello
     * 
     * @return restituire numero di elementi
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * somma  totale dei prodotti nel carrello 
     * 
     * @return restituisce  totale 
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
