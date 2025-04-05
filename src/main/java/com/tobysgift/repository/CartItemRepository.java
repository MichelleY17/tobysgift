package com.tobysgift.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Cart;
import com.tobysgift.model.CartItem;
import com.tobysgift.model.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * per trovare  tutti gli elementi di un carrello
     * 
     * @param cart  carrello
     * @return pestituisce lista di elementi del carrello
     */
    List<CartItem> findByCart(Cart cart);
    
    /**
     * per trovare un elemento del carrello per carrello e prodotto
     * 
     * @param cart carrello
     * @param product prodotto
     * @return restituisce l'elemento del carrello trovato o Optional vuoto
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    /**
     * per eliminare tutti gli elementi di un carrello
     * 
     * @param cart carrello
     */
    void deleteByCart(Cart cart);
    
    /**
     * per contare elementi in un carrello
     * 
     * @param cart carrello
     * @return  numero di elementi nel carrello
     */
    long countByCart(Cart cart);
}
