package com.tobysgift.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Cart;
import com.tobysgift.model.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    

    /**
     * per trovare il carrello di un utente tramite email
     * 
     * @param email email dell'utente proprietario del carrello
     * @return restituisce carrello trovato o Optional vuoto
     */
    Optional<Cart> findByUser_Email(String email);
    
    /**
     * per verificare se esiste un carrello per l'email data
     * 
     * @param email email dell'utente proprietario del carrello
     * @return true = esiste un carrello per questa email // false = non esiste un carrello per questa email
     */
    boolean existsByUser_Email(String email);
    
    // Mantieni i metodi esistenti per compatibilità
    Optional<Cart> findByUser(User user);
    boolean existsByUser(User user);
}