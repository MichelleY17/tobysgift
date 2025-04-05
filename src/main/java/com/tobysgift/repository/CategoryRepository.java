package com.tobysgift.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * per trovare  una categoria per nome
     * 
     * @param nome  nome della categoria
     * @return restituisce categoria trovata o Optional vuoto
     */
    Optional<Category> findByNome(String nome);
    
    /**
     * per verificare  se esiste una categoria con il nome dato
     * 
     * @param nome nome da verificare
     * @return true = esiste una categoria con questo nome // false = non esiste una categoria con questo nome
     */
    boolean existsByNome(String nome);
}