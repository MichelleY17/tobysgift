package com.tobysgift.model;

/**
 * Enum che definisce i possibili stati di un ordine.
 */
public enum OrderStatus {
    CREATO("Creato"),
    PAGAMENTO_IN_ATTESA("Pagamento in attesa"),
    PAGAMENTO_CONFERMATO("Pagamento confermato"),
    IN_PREPARAZIONE("In preparazione"),
    SPEDITO("Spedito"),
    CONSEGNATO("Consegnato"),
    ANNULLATO("Annullato");
    
    private final String descrizione;
    
    OrderStatus(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
}
