package com.tobysgift.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @NotNull
    private BigDecimal totale = BigDecimal.ZERO;
    
    @Column(name = "data_ordine")
    private LocalDateTime dataOrdine;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATO;
    
    @Column(name = "indirizzo_consegna")
    private String indirizzoConsegna;
    
    public Order() {
    }
    
    /**
     * costruttore semplice  utile quando si vuole creare un ordine avendo solo l'informazione dell'utente
     *  L'indirizzo di consegna potrebbe essere impostato successivamente o potrebbe essere utilizzato l'indirizzo predefinito dell'utente
     * 
     * @param user utente che ha effettuato l'ordine
     */
    public Order(User user) {
        this.user = user;
        this.dataOrdine = LocalDateTime.now();
    }
    
    /**
     *costruttore completo utile quando si hanno già tutte le informazioni necessarie per l'ordine,
      compreso l'indirizzo di consegna che potrebbe essere diverso dall'indirizzo predefinito dell'utente
     * 
     * @param user utente che ha effettuato l'ordine
     * @param indirizzoConsegna indirizzo di consegna
     */
    public Order(User user, String indirizzoConsegna) {
        this.user = user;
        this.indirizzoConsegna = indirizzoConsegna;
        this.dataOrdine = LocalDateTime.now();
    }
    
    /**
     * Metodo invocato prima del persist per impostare la data di creazione.
     */
    @PrePersist
    protected void onCreate() {
        if (this.dataOrdine == null) {
            this.dataOrdine = LocalDateTime.now();
        }
    }

    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public BigDecimal getTotale() {
        return totale;
    }
    
    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }
    
    public LocalDateTime getDataOrdine() {
        return dataOrdine;
    }
    
    public void setDataOrdine(LocalDateTime dataOrdine) {
        this.dataOrdine = dataOrdine;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getIndirizzoConsegna() {
        return indirizzoConsegna;
    }
    
    public void setIndirizzoConsegna(String indirizzoConsegna) {
        this.indirizzoConsegna = indirizzoConsegna;
    }
    
    /**
     * Aggiunge un elemento all'ordine
     * 
     * @param item elemento da aggiungere
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calcolaTotale();
    }
    
    /**
     * rimuove un elemento dall'ordine
     * 
     * @param item elemento da rimuovere
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calcolaTotale();
    }
    
    /**
     * Aggiunge un prodotto all'ordine.
     * 
     * @param product Il prodotto da aggiungere
     * @param quantita La quantità
     * @return L'elemento dell'ordine creato
     */
    public OrderItem addProduct(Product product, int quantita) {
        OrderItem orderItem = new OrderItem(this, product, quantita);
        items.add(orderItem);
        calcolaTotale();
        return orderItem;
    }
    
    /**
     * calcola il totale ordine
     */
    public void calcolaTotale() {
        BigDecimal nuovoTotale = BigDecimal.ZERO;
        for (OrderItem item : items) {
            nuovoTotale = nuovoTotale.add(item.getPrezzoTotale());
        }
        this.totale = nuovoTotale;
    }
    
   /**
 * crea un ordine a partire da un carrello usando metodo creaOrdineFromCarello()
 * 
 * @param cart carrello da cui creare l'ordine
 * @param indirizzoConsegna L'indirizzo di consegna
 * @return restituisce nuovo ordine
 */
public static Order creaOrdineFromCarrello(Cart cart, String indirizzoConsegna) {
    if (cart == null) {
        throw new IllegalArgumentException("Il carrello non può essere null");
    }
    
    if (cart.getUser() == null) {
        throw new IllegalArgumentException("Il carrello deve avere un utente valido");
    }
    
    // Crea l'ordine con l'utente e l'indirizzo
    Order order = new Order(cart.getUser(), indirizzoConsegna);
    order.setStatus(OrderStatus.CREATO);
    
    // Aggiungi gli elementi dal carrello all'ordine
    for (CartItem cartItem : cart.getItems()) {
        OrderItem orderItem = new OrderItem(
            order,
            cartItem.getProduct(),
            cartItem.getQuantita(),
            cartItem.getPrezzoUnitario()
        );
        order.getItems().add(orderItem);
    }
    
    // Imposta il totale dell'ordine
    order.setTotale(cart.getTotale());
    
    return order;
}
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", items=" + items.size() +
                ", totale=" + totale +
                ", dataOrdine=" + dataOrdine +
                ", status=" + status +
                '}';
    }
}