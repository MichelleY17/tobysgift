package com.tobysgift.model;


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
