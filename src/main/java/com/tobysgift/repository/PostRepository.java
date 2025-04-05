package com.tobysgift.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Post;
import com.tobysgift.model.PostCategory;
import com.tobysgift.model.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * peer trovare tutti i post di una categoria
     * 
     * @param categoria  categoria dei post
     * @return restituisce lista di post
     */
    List<Post> findByCategoria(PostCategory categoria);
    
    /**
     * per trovare tutti i post di una categoria (paginati)
     * 
     * @param categoria categoria dei post
     * @param pageable Informazioni di paginazione
     * @return Pagina di post
     */
    Page<Post> findByCategoria(PostCategory categoria, Pageable pageable);
    
    /**
     * per trovare i post scritti da un autore
     * 
     * @param autore autore dei post
     * @param pageable Informazioni di paginazione
     * @return Pagina di post
     */
    Page<Post> findByAutore(User autore, Pageable pageable);
    
    /**
     * per cercare  post per titolo o contenuto contenente il termine di ricerca (case insensitive).
     * 
     * @param keyword termine di ricerca
     * @param pageable Informazioni di paginazione
     * @return Pagina di post
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.titolo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.contenuto) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByKeyword(String keyword, Pageable pageable);
    
    /**
     * per cercare  post per titolo o contenuto e categoria
     * 
     * @param keyword  termine di ricerca
     * @param categoria categoria dei post
     * @param pageable Informazioni di paginazione
     * @return Pagina di post
     */
    @Query("SELECT p FROM Post p WHERE (LOWER(p.titolo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.contenuto) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.categoria = :categoria")
    Page<Post> searchByKeywordAndCategoria(String keyword, PostCategory categoria, Pageable pageable);
    
    /**
     * per trovare i post più recenti
     * 
     * @param limit numero massimo di post da recuperare
     * @return restituisce lista di post
     */
    @Query("SELECT p FROM Post p ORDER BY p.dataCreazione DESC")
    List<Post> findRecentPosts(Pageable pageable);
    
    /**
     * per trovare i post più recenti per categoria
     * 
     * @param categoria categoria dei post
     * @param limit numero massimo di post da recuperare
     * @return restituisce lista di post
     */
    @Query("SELECT p FROM Post p WHERE p.categoria = :categoria ORDER BY p.dataCreazione DESC")
    List<Post> findRecentPostsByCategoria(PostCategory categoria, Pageable pageable);
}
