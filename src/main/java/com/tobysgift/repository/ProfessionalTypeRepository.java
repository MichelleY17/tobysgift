package com.tobysgift.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.ProfessionalType;

@Repository
public interface ProfessionalTypeRepository extends JpaRepository<ProfessionalType, Long> {
    
    /**
     * Trova un tipo di professionista per nome
     * 
     * @param nome nome del tipo
     * @return restituiece tipo trovato o Optional vuoto
     */
    Optional<ProfessionalType> findByNome(String nome);
    
    /**
     * per verificare se esiste un tipo di professionista con il nome dato
     * 
     * @param nome nome da verificare
     * @return true = esiste un tipo con questo nome // false = non esiste un tipo con questo nome
     */
    boolean existsByNome(String nome);
}
