package com.tobysgift.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Appointment;
import com.tobysgift.model.AppointmentStatus;
import com.tobysgift.model.Professional;
import com.tobysgift.model.User;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * per trovare tutti gli appuntamenti di un utente ordinati per data (dal più recente)
     * 
     * @param user utente
     * @return restituisce  lista di appuntamenti
     */
    List<Appointment> findByUserOrderByDataOraDesc(User user);
    
    /**
     * per trovare tutti gli appuntamenti di un utente (paginati)
     * 
     * @param user utente
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByUser(User user, Pageable pageable);
    
    /**
     * per trovare  tutti gli appuntamenti di un professionista ordinati per data
     * 
     * @param professional professionista
     * @return restituisce lista di appuntamenti
     */
    List<Appointment> findByProfessionalOrderByDataOra(Professional professional);
    
    /**
     * per trovare tutti gli appuntamenti di un professionista (paginati)
     * 
     * @param professional professionista
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByProfessional(Professional professional, Pageable pageable);
    
    /**
     * per trovare appuntamenti per stato
     * 
     * @param status  stato degli appuntamenti
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    
    /**
     * peer trovare appuntamenti di un utente per stato
     * 
     * @param user utente
     * @param status Lo stato degli appuntamenti
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByUserAndStatus(User user, AppointmentStatus status, Pageable pageable);
    
    /**
     * per trovare appuntamenti di un professionista per stato
     * 
     * @param professional professionista
     * @param status Lo stato degli appuntamenti
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByProfessionalAndStatus(Professional professional, AppointmentStatus status, Pageable pageable);
    
    /**
     * Trova appuntamenti pianificati in un periodo di tempo.
     * 
     * @param da Data di inizio
     * @param a Data di fine
     * @return Lista di appuntamenti
     */
    List<Appointment> findByDataOraBetweenOrderByDataOra(LocalDateTime da, LocalDateTime a);
    
    /**
     * per torvare  appuntamenti futuri di un professionista
     * 
     * @param professional  professionista
     * @param now Data corrente
     * @param pageable Informazioni di paginazione
     * @return Pagina di appuntamenti
     */
    Page<Appointment> findByProfessionalAndDataOraGreaterThanEqualOrderByDataOra(Professional professional, LocalDateTime now, Pageable pageable);
    
    /**
     * per verificare  se c'è già un appuntamento per un professionista in una determinata fascia oraria
     * 
     * @param professional  professionista
     * @param inizio Ora di inizio
     * @param fine Ora di fine
     * @return true = esiste già un appuntamento per questo professionista nella fascia oraria // false= non esiste già un appuntamento per questo professionista nella fascia oraria
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.professional = :professional AND a.dataOra BETWEEN :inizio AND :fine AND a.status <> 'ANNULLATO'")
    boolean existsByProfessionalAndTimeSlot(@Param("professional") Professional professional, @Param("inizio") LocalDateTime inizio, @Param("fine") LocalDateTime fine);
    
    /**
     * per contare gli appuntamenti per stato
     * 
     * @param status  stato degli appuntamenti
     * @return Il numero di appuntamenti con lo stato specificato
     */
    long countByStatus(AppointmentStatus status);
}
