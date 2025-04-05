package com.tobysgift.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "post_categories")
public class PostCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Il nome della categoria è obbligatorio")
    @Column(unique = true)
    private String nome;
    
    private String descrizione;
    
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();
    

    public PostCategory() {
    }
    
    /**
     * Costruttore con parametri
     * 
     * @param nome nome della categoria
     * @param descrizione descrizione della categoria
     */
    public PostCategory(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }
    

    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public List<Post> getPosts() {
        return posts;
    }
    
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    
    /**
     *  per aggiungere un post alla categoria
     * 
     * @param post post da aggiungere
     */
    public void addPost(Post post) {
        posts.add(post);
        post.setCategoria(this);
    }
    
    /**
     * per rimuovere un post dalla categoria
     * 
     * @param post post da rimuovere
     */
    public void removePost(Post post) {
        posts.remove(post);
        post.setCategoria(null);
    }
    
    @Override
    public String toString() {
        return "PostCategory{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}