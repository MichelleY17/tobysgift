package com.tobysgift.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.tobysgift.model.Category;
import com.tobysgift.model.Product;

public interface ProductService {
    
    /**
     * per trovare tutti i prodotti 
     * 
     * @return restituisce lista di tutti i prodotti
     */
    List<Product> findAll();
    
    /**
     * per trovare  tutti i prodotti (paginati)
     * 
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> findAll(Pageable pageable);
    
    /**
     * per trovare  un prodotto per ID
     * 
     * @param id ID del prodotto
     * @return restituisce ilprodotto trovato o Optional vuoto
     */
    Optional<Product> findById(Long id);
    
    /**
     * per trovare  prodotti per categoria
     * 
     * @param categoryId ID della categoria
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> findByCategory(Long categoryId, Pageable pageable);
    
    /**
     * per cercare prodotti per nome o descrizione
     * 
     * @param keyword  termine di ricerca
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> searchProducts(String keyword, Pageable pageable);
    
    /**
     * per cercare prodotti per nome o descrizione e categoria
     * 
     * @param keyword termine di ricerca
     * @param categoryId ID della categoria
     * @param pageable Informazioni di paginazione
     * @return Pagina di prodotti
     */
    Page<Product> searchProductsByCategory(String keyword, Long categoryId, Pageable pageable);
    
    /**
     * per salvare un nuovo prodotto
     * 
     * @param product prodotto da salvare
     * @param imageFile immagine del prodotto (può essere null)
     * @return restituisce il prodotto salvato
     */
    Product save(Product product, MultipartFile imageFile);
    
    /**
     * per aggiornare un prodotto esistente
     * 
     * @param product prodotto con i dati aggiornati
     * @param imageFile La nuova immagine del prodotto (può essere null per mantenere quella esistente)
     * @return restituisce il prodotto aggiornato
     */
    Product update(Product product, MultipartFile imageFile);
    
    /**
     *per eliminare un prodotto
     * 
     * @param id ID del prodotto
     * @return true = il prodotto è stato eliminato // false = il prodotto non è stato eliminato
     */
    boolean delete(Long id);
    
    /**
     * per trovare prodotti con quantità disponibile bassa
     * 
     * @param threshold soglia di quantità
     * @return restituisce lista di prodotti
     */
    List<Product> findLowStockProducts(int threshold);
    
    /**
     * per aggiornare la quantità disponibile di un prodotto
     * 
     * @param productId ID del prodotto
     * @param quantity nuova quantità
     * @return restituisce il  prodotto aggiornato o null se non è stato trovato
     */
    Product updateQuantity(Long productId, int quantity);
    
    /**
     * per verificare se c'è disponibilità per una certa quantità di un prodotto
     * 
     * @param productId L'ID del prodotto
     * @param quantityRequested La quantità richiesta
     * @return true = c'è disponibilità // false = non c'è disponibilità
     */
    boolean isProductAvailable(Long productId, int quantityRequested);
    
    /**
     * per trovare tutti i prodotti di una certa categoria
     * 
     * @param categoryName  nome della categoria
     * @return restituisce lista di prodotti
     */
    List<Product> findByCategoryName(String categoryName);
    
    /**
     * per trovare un numero limitato di prodotti di una certa categoria
     * 
     * @param categoryName nome della categoria
     * @param page  numero di pagina
     * @param size dimensione della pagina
     * @return restituisce lista di prodotti
     */
    List<Product> findByCategoryName(String categoryName, int page, int size);
    
    /**
     * per trovare  prodotti in evidenza (con implementazione a scelta)
     * 
     * @param limit numero massimo di prodotti
     * @return restituisce lista di prodotti
     */
    List<Product> findFeaturedProducts(int limit);
    
    /**
     * per trovare gli ultimi prodotti aggiunti
     * 
     * @param limit numero massimo di prodotti
     * @return restituisce lista di prodotti
     */
    List<Product> findRecentProducts(int limit);
    
    /**
     * per contare il numero totale di prodotti
     * 
     * @return restituisce  numero di prodotti
     */
    long countProducts();
    
    /**
     * per trovare tutte le categorie
     * 
     * @return restituisce lista di categorie
     */
    List<Category> findAllCategories();
    
    /**
     * per trovare una categoria per ID
     * 
     * @param id ID della categoria
     * @return restituisce  categoria trovata o Optional vuoto
     */
    Optional<Category> findCategoryById(Long id);
    
    /**
     * per trovare una categoria per nome
     * 
     * @param name  nome della categoria
     * @return restituisce categoria trovata o Optional vuoto
     */
    Optional<Category> findCategoryByName(String name);
    
    /**
     * per slavare una nuova categoria
     * 
     * @param category La categoria da salvare
     * @return La categoria salvata
     */
    Category saveCategory(Category category);
    
    /**
     * per eliminare una categoria
     * 
     * @param id ID della categoria
     * @return true = la categoria è stata eliminata // false = se non è stata trovata
     */
    boolean deleteCategory(Long id);
}
