package com.tobysgift.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.PostCategory;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    
    /**
     * per trovare  una categoria di post per nome
     * 
     * @param nome nome della categoria
     * @return categoria trovata o Optional vuoto
     */
    Optional<PostCategory> findByNome(String nome);
    
    /**
     * per verificare se esiste una categoria di post con il nome dato
     * 
     * @param nome ome da verificare
     * @return true =  esiste una categoria con questo nome //false = esiste una categoria con questo nome
     */
    boolean existsByNome(String nome);
}