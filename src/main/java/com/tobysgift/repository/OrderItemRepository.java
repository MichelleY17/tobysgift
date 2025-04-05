package com.tobysgift.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Order;
import com.tobysgift.model.OrderItem;
import com.tobysgift.model.Product;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * per trovare  tutti gli elementi di un ordine
     * 
     * @param order ordine
     * @return restituisce lista di elementi dell'ordine
     */
    List<OrderItem> findByOrder(Order order);
    
    /**
     * per trovare  tutti gli elementi che contengono un determinato prodotto
     * 
     * @param product prodotto
     * @return restituisce lista di elementi dell'ordine
     */
    List<OrderItem> findByProduct(Product product);
    
    /**
     * Cper contare  quante volte un prodotto è stato ordinato
     * 
     * @param productId ID del prodotto
     * @return restituisce il numero di volte che il prodotto è stato ordinato
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.id = :productId")
    long countOrdersByProduct(@Param("productId") Long productId);
    
    /**
     * per calcolare  la quantità totale venduta di un prodotto.
     * 
     * @param productId ID del prodotto
     * @return restituisce la quantità totale venduta
     */
    @Query("SELECT SUM(oi.quantita) FROM OrderItem oi WHERE oi.product.id = :productId")
    Integer getTotalQuantitySoldByProduct(@Param("productId") Long productId);
    
    /**
     * per trovar ei prodotti più venduti.
     * 
     * @param limit numero massimo di prodotti da recuperare
     * @return restituisce lista di oggetti contenenti productId e totalQuantity
     */
    @Query("SELECT oi.product.id as productId, SUM(oi.quantita) as totalQuantity " +
           "FROM OrderItem oi GROUP BY oi.product.id ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
