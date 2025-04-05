package com.tobysgift.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tobysgift.model.Category;
import com.tobysgift.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * per trovare prodotti per categoria
     * 
     * @param categoria  categoria dei prodotti
     * @return restituisce lista di prodotti
     */
    List<Product> findByCategoria(Category categoria);
    
    /**
     * per trovare prodotti per categoria (paginati)
     * 
     * @param categoria categoria dei prodotti
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> findByCategoria(Category categoria, Pageable pageable);
    
    /**
     * per trovare  prodotti per nome contenente il termine di ricerca (case insensitive)
     * 
     * @param nome termine di ricerca per il nome
     * @param pageable Informazioni di paginazione
     * @return restituisce pagina di prodotti
     */
    Page<Product> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    /**
     *per cercare  prodotti per nome e categoria
     * 
     * @param nome termine di ricerca per il nome
     * @param categoria categoria dei prodotti
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> findByNomeContainingIgnoreCaseAndCategoria(String nome, Category categoria, Pageable pageable);
    
    /**
     * per trovare prodotti con quantità disponibile bassa
     * 
     * @param soglia oglia di quantità
     * @return lista di prodotti con quantità minore o uguale alla soglia
     */
    List<Product> findByQuantitaDisponibileLessThanEqual(int soglia);
    
    /**
     * per trovare prodotti con quantità disponibile bassa (paginati).
     * 
     * @param soglia soglia di quantità
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> findByQuantitaDisponibileLessThanEqual(int soglia, Pageable pageable);
    
    /**
     * per ricerca  avanzata di prodotti.
     * 
     * @param keyword Parola chiave per la ricerca (nome o descrizione)
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.descrizione) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * per ricerca  avanzata di prodotti con filtro per categoria.
     * 
     * @param keyword Parola chiave per la ricerca (nome o descrizione)
     * @param categoriaId ID della categoria
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.nome) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.descrizione) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.categoria.id = :categoriaId")
    Page<Product> searchByKeywordAndCategoriaId(@Param("keyword") String keyword, @Param("categoriaId") Long categoriaId, Pageable pageable);
}