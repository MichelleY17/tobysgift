package com.tobysgift.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Entità che rappresenta un post del blog.
 */
@Entity
@Table(name = "posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il titolo è obbligatorio")
    private String titolo;
    
    @NotBlank(message = "Il contenuto è obbligatorio")
    @Column(columnDefinition = "TEXT")
    private String contenuto;
    
    private String immagine;
    
    @Column(name = "data_creazione")
    private LocalDateTime dataCreazione;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autore_id", nullable = false)
    private User autore;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private PostCategory categoria;
    
    /**
     * Costruttore di default.
     */
    public Post() {
    }
    
    /**
     * Costruttore con parametri principali.
     * 
     * @param titolo Il titolo del post
     * @param contenuto Il contenuto del post
     * @param autore L'autore del post
     * @param categoria La categoria del post
     */
    public Post(String titolo, String contenuto, User autore, PostCategory categoria) {
        this.titolo = titolo;
        this.contenuto = contenuto;
        this.autore = autore;
        this.categoria = categoria;
        this.dataCreazione = LocalDateTime.now();
    }
    
    /**
     * Metodo invocato prima del persist per impostare la data di creazione.
     */
    @PrePersist
    protected void onCreate() {
        if (this.dataCreazione == null) {
            this.dataCreazione = LocalDateTime.now();
        }
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitolo() {
        return titolo;
    }
    
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }
    
    public String getContenuto() {
        return contenuto;
    }
    
    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }
    
    public String getImmagine() {
        return immagine;
    }
    
    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }
    
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }
    
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
    
    public User getAutore() {
        return autore;
    }
    
    public void setAutore(User autore) {
        this.autore = autore;
    }
    
    public PostCategory getCategoria() {
        return categoria;
    }
    
    public void setCategoria(PostCategory categoria) {
        this.categoria = categoria;
    }
    
    /**
     * Ottiene un estratto del contenuto per l'anteprima.
     * 
     * @param length La lunghezza massima dell'estratto
     * @return Un estratto del contenuto
     */
    public String getEstratto(int length) {
        if (contenuto.length() <= length) {
            return contenuto;
        }
        return contenuto.substring(0, length) + "...";
    }
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", titolo='" + titolo + '\'' +
                ", dataCreazione=" + dataCreazione +
                ", autore=" + (autore != null ? autore.getUsername() : "null") +
                ", categoria=" + (categoria != null ? categoria.getNome() : "null") +
                '}';
    }
}