package com.tobysgift.model;

/**
 * Enum che definisce i possibili stati di un appuntamento.
 */
public enum AppointmentStatus {
    RICHIESTO("Richiesto"),
    CONFERMATO("Confermato"),
    COMPLETATO("Completato"),
    ANNULLATO("Annullato");
    
    private final String descrizione;
    
    AppointmentStatus(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
}