package com.tobysgift.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Professional;
import com.tobysgift.model.ProfessionalType;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    
    /**
     * per trovare  un professionista per email
     * 
     * @param email mail del professionista
     * @return restituisce professionista trovato o Optional vuoto
     */
    Optional<Professional> findByEmail(String email);
    
    /**
     * pertrovare  professionisti per tipo
     * 
     * @param tipo tipo di professionista
     * @return  restituisce lista di professionisti
     */
    List<Professional> findByTipo(ProfessionalType tipo);
    
    /**
     * per trova professionisti per tipo (paginati)
     * 
     * @param tipo tipo di professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> findByTipo(ProfessionalType tipo, Pageable pageable);
    
    /**
     * per cercare professionisti per nome o cognome
     * 
     * @param keyword termine di ricerca
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    @Query("SELECT p FROM Professional p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.cognome) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Professional> searchByKeyword(String keyword, Pageable pageable);
    
    /**
     * per cercare professionisti per nome o cognome e tipo
     * 
     * @param keyword  termine di ricerca
     * @param tipo tipo di professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    @Query("SELECT p FROM Professional p WHERE (LOWER(p.nome) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.cognome) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.tipo = :tipo")
    Page<Professional> searchByKeywordAndTipo(String keyword, ProfessionalType tipo, Pageable pageable);
    
    /**
     * per trovare professionisti per specializzazione
     * 
     * @param specializzazione specializzazione
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> findBySpecializzazioneContainingIgnoreCase(String specializzazione, Pageable pageable);
    
    /**
     * per prenotare i professionisti più prenotati
     * 
     * @param pageable Informazioni di paginazione
     * @return restituisce lista di professionisti
     */
    @Query("SELECT p FROM Professional p JOIN p.appointments a GROUP BY p ORDER BY COUNT(a) DESC")
    List<Professional> findMostBookedProfessionals(Pageable pageable);
}