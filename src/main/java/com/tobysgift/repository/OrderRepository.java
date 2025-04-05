package com.tobysgift.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Order;
import com.tobysgift.model.OrderStatus;
import com.tobysgift.model.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * per trovare  tutti gli ordini di un utente ordinati per data (dal più recente).
     * 
     * @param user utente proprietario degli ordini
     * @return restituisce lista di ordini
     */
    List<Order> findByUserOrderByDataOrdineDesc(User user);
    
    /**
     * per trovare tutti gli ordini di un utente (paginati)
     * 
     * @param user utente proprietario degli ordini
     * @param pageable Informazioni di paginazione
     * @return Pagina di ordini
     */
    Page<Order> findByUser(User user, Pageable pageable);
    
    /**
     * per trovare  ordini per stato.
     * 
     * @param status stato degli ordini
     * @param pageable Informazioni di paginazione
     * @return Pagina di ordini
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * per trovare  ordini creati in un periodo di tempo
     * 
     * @param da Data di inizio
     * @param a Data di fine
     * @param pageable Informazioni di paginazione
     * @return Pagina di ordini
     */
    Page<Order> findByDataOrdineBetween(LocalDateTime da, LocalDateTime a, Pageable pageable);
    
    /**
     * per trovare  gli ordini più recenti.
     * 
     * @param limit numero massimo di ordini da recuperare
     * @return per ista di ordini
     */
    @Query("SELECT o FROM Order o ORDER BY o.dataOrdine DESC")
    List<Order> findRecentOrders(Pageable pageable);
    
    /**
     * per contare  gli ordini per stato
     * 
     * @param status Lo stato degli ordini
     * @return restituisce il numero di ordini con lo stato specificato
     */
    long countByStatus(OrderStatus status);
}