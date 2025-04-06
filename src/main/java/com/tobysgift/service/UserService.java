package com.tobysgift.service;

import java.util.Optional;

import com.tobysgift.model.User;

public interface UserService {
    
    /**
     * per la registrazione di un nuovo utente
     * 
     * @param user utente da registrare
     * @return restittuisce utente registrato
     */
    User register(User user);
    
    /**
     * per aggiornare  il profilo di un utente
     * 
     * @param user utente con i dati aggiornati
     * @return restituisce utente aggiornato
     */
    User updateProfile(User user);
    
    /**
     * per cambiare la password di un utente
     * 
     * @param email email dell'utente
     * @param oldPassword  vecchia password
     * @param newPassword nuova password
     * @return true = la password è stata cambiata con successo // false = la password  non è stata cambiata con successo
     */
    boolean changePassword(String email, String oldPassword, String newPassword);
    
    /**
     * per camniare il nome utente
     * 
     * @param email email dell'utente
     * @param newUsername  nuovo nome utente
     * @return true = il nome utente è stato cambiato con successo // false = il nome utente non è stato cambiato con successo
     */
    boolean changeUsername(String email, String newUsername);
    
    /**
     * per trovare  un utente per email
     * 
     * @param email email dell'utente
     * @return restituisce utente trovato o null
     */
    User findByEmail(String email);
    
    /**
     * per trovare un utente per nome utente
     * 
     * @param username nome utente
     * @return restituisce utente trovato o null
     */
    User findByUsername(String username);
    
    /**
     * per trovare  un utente per ID
     * 
     * @param id ID dell'utente
     * @return restituisce l'utente trovato o Optional vuoto
     */
    Optional<User> findById(Long id);
    
    /**
     * per verificare se esiste un utente con l'email data
     * 
     * @param email email da verificare
     * @return true =  esiste un utente con questa email // false = non  esiste un utente con questa email
     */
    boolean existsByEmail(String email);
    
    /**
     * per verificare se esiste un utente con il nome utente dato
     * 
     * @param username nome utente da verificare
     * @return true = esiste un utente con questo nome utente //false = non esiste un utente con questo nome utente
     */
    boolean existsByUsername(String username);
    
    /**
     * per aggiornare la data dell'ultimo accesso di un utente
     * 
     * @param email email dell'utente
     */
    void updateLastLogin(String email);
}