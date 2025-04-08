package com.tobysgift.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.tobysgift.model.Professional;
import com.tobysgift.model.ProfessionalType;

public interface ProfessionalService {
    
    /**
     * per trovare tutti i professionisti 
     * 
     * @return restituisce lista di tutti i professionisti
     */
    List<Professional> findAll();
    
    /**
     * per trovare tutti i professionisti (paginati)
     * 
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> findAll(Pageable pageable);
    
    /**
     * per trovare un professionista per ID
     * 
     * @param id ID del professionista
     * @return restituisce il professionista trovato o Optional vuoto
     */
    Optional<Professional> findById(Long id);
    
    /**
     * per trovare professionisti per tipo
     * 
     * @param typeId ID del tipo di professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> findByType(Long typeId, Pageable pageable);
    
    /**
     * per cercare professionisti per nome o cognome
     * 
     * @param keyword termine di ricerca
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> searchProfessionals(String keyword, Pageable pageable);
    
    /**
     * per cercare professionisti per nome o cognome e tipo
     * 
     * @param keyword termine di ricerca
     * @param typeId ID del tipo di professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> searchProfessionalsByType(String keyword, Long typeId, Pageable pageable);
    
    /**
     * per salvare un nuovo professionista
     * 
     * @param professional professionista da salvare
     * @param imageFile immagine del professionista (può essere null)
     * @return restituisce il professionista salvato
     */
    Professional save(Professional professional, MultipartFile imageFile);
    
    /**
     * per aggiornare un professionista esistente
     * 
     * @param professional professionista con i dati aggiornati
     * @param imageFile La nuova immagine del professionista (può essere null per mantenere quella esistente)
     * @return restituisce il professionista aggiornato
     */
    Professional update(Professional professional, MultipartFile imageFile);
    
    /**
     * per eliminare un professionista
     * 
     * @param id ID del professionista
     * @return true = il professionista è stato eliminato // false = il professionista non è stato eliminato
     */
    boolean delete(Long id);
    
    /**
     * per trovare i professionisti più prenotati
     * 
     * @param limit numero massimo di professionisti
     * @return restituisce lista di professionisti
     */
    List<Professional> findMostBookedProfessionals(int limit);
    
    /**
     * per trovare professionisti per specializzazione
     * 
     * @param specializzazione specializzazione da cercare
     * @param pageable Informazioni di paginazione
     * @return Pagina di professionisti
     */
    Page<Professional> findBySpecialization(String specializzazione, Pageable pageable);
    
    /**
     * per contare il numero totale di professionisti
     * 
     * @return restituisce numero di professionisti
     */
    long countProfessionals();
    
    /**
     * per trovare tutti i tipi di professionisti
     * 
     * @return restituisce lista di tipi di professionisti
     */
    List<ProfessionalType> findAllProfessionalTypes();
    
    /**
     * per trovare un tipo di professionista per ID
     * 
     * @param id ID del tipo di professionista
     * @return restituisce tipo di professionista trovato o Optional vuoto
     */
    Optional<ProfessionalType> findProfessionalTypeById(Long id);
    
    /**
     * per trovare un tipo di professionista per nome
     * 
     * @param name nome del tipo di professionista
     * @return restituisce tipo di professionista trovato o Optional vuoto
     */
    Optional<ProfessionalType> findProfessionalTypeByName(String name);
    
    /**
     * per salvare un nuovo tipo di professionista
     * 
     * @param professionalType Il tipo di professionista da salvare
     * @return Il tipo di professionista salvato
     */
    ProfessionalType saveProfessionalType(ProfessionalType professionalType);
    
    /**
     * per eliminare un tipo di professionista
     * 
     * @param id ID del tipo di professionista
     * @return true = il tipo è stato eliminato // false = se non è stato trovato
     */
    boolean deleteProfessionalType(Long id);
}