package com.tobysgift.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tobysgift.model.Order;
import com.tobysgift.model.User;

public interface OrderService {
    
    /**
     * Crea un nuovo ordine dal carrello dell'utente
     * 
     * @param user Utente che effettua l'ordine
     * @param indirizzoConsegna Indirizzo di consegna
     * @return L'ordine creato
     */
    Order createOrderFromCart(User user, String indirizzoConsegna);
    
    /**
     * Trova un ordine per ID
     * 
     * @param id ID dell'ordine
     * @return Ordine trovato o Optional vuoto
     */
    Optional<Order> findById(Long id);
    
    /**
     * Trova tutti gli ordini di un utente
     * 
     * @param user Utente proprietario degli ordini
     * @return Lista di ordini
     */
    List<Order> findOrdersByUser(User user);
    
    /**
     * Trova tutti gli ordini di un utente (paginati)
     * 
     * @param user Utente proprietario degli ordini
     * @param pageable Informazioni di paginazione
     * @return Pagina di ordini
     */
    Page<Order> findOrdersByUser(User user, Pageable pageable);
    
    /**
     * Trova tutti gli ordini (paginati) - per admin
     * 
     * @param pageable Informazioni di paginazione
     * @return Pagina di ordini
     */
    Page<Order> findAllOrders(Pageable pageable);
    
    /**
     * Verifica se un utente è autorizzato ad accedere a un ordine
     * 
     * @param orderId ID dell'ordine
     * @param user Utente da verificare
     * @return true se l'utente è autorizzato, false altrimenti
     */
    boolean isUserAuthorized(Long orderId, User user);
    
    /**
     * Salva un ordine esistente (per aggiornamenti)
     * 
     * @param order Ordine da salvare
     * @return Ordine salvato
     */
    Order saveOrder(Order order);
}