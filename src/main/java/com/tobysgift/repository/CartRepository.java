package com.tobysgift.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Cart;
import com.tobysgift.model.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * per trovare  il carrello di un utente
     * 
     * @param user utente proprietario del carrello
     * @return restituisce carrello trovato o Optional vuoto
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * per verifivare  se esiste un carrello per l'utente dato
     * 
     * @param user utente proprietario del carrello
     * @return true = esiste un carrello per questo utente // false =  non esiste un carrello per questo utente
     */
    boolean existsByUser(User user);
}