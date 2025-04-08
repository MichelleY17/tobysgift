package com.tobysgift.service;

import java.math.BigDecimal;
import java.util.List;

import com.tobysgift.model.Cart;
import com.tobysgift.model.CartItem;
import com.tobysgift.model.Product;
import com.tobysgift.model.User;

public interface CartService {
    
    /**
     * per  ottenere il carrello di un utente, e se non esiste crrarne uno nuovo
     * 
     * @param user utente proprietario del carrello
     * @return restituisce il carrello dell'utente
     */
    Cart getCartByUser(User user);
    
    /**
     * per aggiungere un prodott nel carrello
     * 
     * @param user utente proprietario del carrello
     * @param productId ID del prodotto da aggiungere
     * @param quantity quantità da aggiungere
     * @return restituisce l'elemento del carrello creato o aggiornato
     */
    CartItem addProductToCart(User user, Long productId, int quantity);
    
    /**
     * per aggiornare la quantita di un prodotto nel carrello
     * 
     * @param user utente proprietario del carrello
     * @param cartItemId ID dell'elemento del carrello
     * @param quantity nuova quantità
     * @return restituisce l'elemento del carrello aggiornato
     */
    CartItem updateCartItemQuantity(User user, Long cartItemId, int quantity);
    
    /**
     * per rimuovere il prodotto del carrello
     * 
     * @param user utente proprietario del carrello
     * @param cartItemId ID dell'elemento del carrello
     * @return true =l'elemento è stato rimosso //false = l'elemento non  è stato rimosso
     */
    boolean removeCartItem(User user, Long cartItemId);
    
    /**
     * per svuotare il carrello di un utente
     * 
     * @param user utente proprietario del carrello
     */
    void clearCart(User user);
    
    /**
     *per contare il numero di articoli nel carrello
     * 
     * @param user utente proprietario del carrello
     * @return restituisce il numero di articoli
     */
    int getCartItemCount(User user);
    
    /**
     * per calcolare il totale di un elemento nel carrello
     * 
     * @param cartItemId ID dell'elemento del carrello
     * @return  totale dell'elemento
     */
    BigDecimal getCartItemTotal(Long cartItemId);
    
    /**
     *per verificare se un prodotto è gia nel carreloo
     * 
     * @param user utente proprietario del carrello
     * @param productId ID del prodotto
     * @return true = il prodotto è nel carrello // false = il prodotto non è nel carrello
     */
    boolean isProductInCart(User user, Long productId);
    
    /**
     * per ottenere l'elemento del carrello per uno specifico prodotto
     * 
     * @param user utente proprietario del carrello
     * @param productId ID del prodotto
     * @return restituisce l'elemento del carrello o null se non trovato
     */
    CartItem getCartItemByProduct(User user, Long productId);
    
    /**
     * per ottenre  la lista di prodotti correlati in base agli elementi nel carrello
     * 
     * @param user utente proprietario del carrello
     * @param limit numero massimo di prodotti da recuperare
     * @return restituisce lista di prodotti correlati
     */
    List<Product> getRelatedProducts(User user, int limit);
}