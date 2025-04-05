package com.tobysgift.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * trova un utente per email
     * 
     * @param email email dell'utente
     * @return restituisce utente trovato o Optional vuoto
     */
    Optional<User> findByEmail(String email);
    
    /**
     * trova un utente per username
     * 
     * @param username  nome utente
     * @return utente trovato o Optional vuoto
     */
    Optional<User> findByUsername(String username);
    
    /**
     * per verificare  se esiste un utente con l'email data
     * 
     * @param email email da verificare
     * @return true =  esiste un utente con questa email // false = non esiste un utente con questa email
     */
    boolean existsByEmail(String email);
    
    /**
     * per verificare se esiste un utente con l'username dato
     * 
     * @param username  nome utente da verificare
     * @return true = esiste un utente con questo username //false = non esiste un utente con questo username
     */
    boolean existsByUsername(String username);
}
