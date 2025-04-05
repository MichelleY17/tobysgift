package com.tobysgift.model;

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