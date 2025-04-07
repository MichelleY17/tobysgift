package com.tobysgift.service;

import java.math.BigDecimal;

import com.tobysgift.model.Cart;
import com.tobysgift.model.CartItem;
import com.tobysgift.model.User;


public interface CartService {
    
    /**
     * per ottenere  il carrello di un utente, altrimenti creandolo se non esiste
     * 
     * @param user utente proprietario del carrello
     * @return restituisce il carrello dell'utente
     */
    Cart getCartByUser(User user);
    
    /**
     * per aggiungere un prodotto al carrello dell'utente
     * 
     * @param user utente proprietario del carrello
     * @param productId ID del prodotto da aggiungere
     * @param quantity  quantità da aggiungere
     * @return restituisce l'elemento del carrello creato o aggiornato
     * @throws RuntimeException se il prodotto non è disponibile nella quantità richiesta
     */
    CartItem addProductToCart(User user, Long productId, int quantity);
    
    /**
     * per aggiornare la quantità di un prodotto nel carrello
     * 
     * @param user utente proprietario del carrello
     * @param cartItemId ID dell'elemento del carrello
     * @param quantity  nuova quantità
     * @return restituisce l'elemento del carrello aggiornato
     * @throws RuntimeException se il prodotto non è disponibile nella quantità richiesta
     */
    CartItem updateCartItemQuantity(User user, Long cartItemId, int quantity);
    
    /**
     * per rimuovere un elemento dal carrello
     * 
     * @param user utente proprietario del carrello
     * @param cartItemId ID dell'elemento del carrello
     * @throws RuntimeException se l'elemento non appartiene al carrello dell'utente
     */
    void removeCartItem(User user, Long cartItemId);
    
    /**
     * per svuotare il carrello dell'utente
     * 
     * @param user utente proprietario del carrello
     */
    void clearCart(User user);
    
    /**
     * per calcolare il totale del carrello
     * 
     * @param cart carrello
     * @return restituisce il totale del carrello
     */
    BigDecimal calculateCartTotal(Cart cart);
    
    /**
     * per ottenere il numero di elementi nel carrello dell'utente
     * 
     * @param user utente proprietario del carrello
     * @return restituisce il numero di elementi
     */
    int getCartItemCount(User user);
    
    /**
     * per ottenere il totale di un elemento del carrello (prezzo * quantità)
     * 
     * @param cartItemId ID dell'elemento del carrello
     * @return restituisce il totale dell'elemento
     */
    BigDecimal getCartItemTotal(Long cartItemId);
    
    /**
     * per verificare se un prodotto è già nel carrello dell'utente
     * 
     * @param user utente proprietario del carrello
     * @param productId ID del prodotto
     * @return restituisce  true =  il prodotto è già nel carrello // false = il prodotto non è già nel carrello
     */
    boolean isProductInCart(User user, Long productId);
}