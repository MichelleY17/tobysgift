package com.tobysgift.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tobysgift.model.Category;
import com.tobysgift.model.Product;
import com.tobysgift.repository.CategoryRepository;
import com.tobysgift.repository.ProductRepository;
import com.tobysgift.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    @Override
    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        return category.map(value -> productRepository.findByCategoria(value, pageable))
                      .orElseGet(() -> Page.empty(pageable));
    }
    
    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable);
    }
    
    @Override
    public Page<Product> searchProductsByCategory(String keyword, Long categoryId, Pageable pageable) {
        return productRepository.searchByKeywordAndCategoriaId(keyword, categoryId, pageable);
    }
    
    @Override
    @Transactional
    public Product save(Product product, MultipartFile imageFile) {
        // Salva l'immagine se presente
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = saveImage(imageFile);
            product.setImmagine(imageName);
        }
        
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product update(Product product, MultipartFile imageFile) {
        // Recupera il prodotto esistente
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato con ID: " + product.getId()));
        
        // Aggiorna i campi
        existingProduct.setNome(product.getNome());
        existingProduct.setDescrizione(product.getDescrizione());
        existingProduct.setPrezzo(product.getPrezzo());
        existingProduct.setQuantitaDisponibile(product.getQuantitaDisponibile());
        existingProduct.setCategoria(product.getCategoria());
        
        // Aggiorna l'immagine solo se ne è stata caricata una nuova
        if (imageFile != null && !imageFile.isEmpty()) {
            // Elimina la vecchia immagine se presente
            if (existingProduct.getImmagine() != null) {
                deleteImage(existingProduct.getImmagine());
            }
            
            // Salva la nuova immagine
            String imageName = saveImage(imageFile);
            existingProduct.setImmagine(imageName);
        }
        
        return productRepository.save(existingProduct);
    }
    
    @Override
    @Transactional
    public boolean delete(Long id) {
        Optional<Product> product = productRepository.findById(id);
        
        if (product.isPresent()) {
            // Elimina l'immagine se presente
            if (product.get().getImmagine() != null) {
                deleteImage(product.get().getImmagine());
            }
            
            productRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findByQuantitaDisponibileLessThanEqual(threshold);
    }
    
    @Override
    @Transactional
    public Product updateQuantity(Long productId, int quantity) {
        Optional<Product> product = productRepository.findById(productId);
        
        if (product.isPresent()) {
            product.get().setQuantitaDisponibile(quantity);
            return productRepository.save(product.get());
        }
        
        return null;
    }
    
    @Override
    public boolean isProductAvailable(Long productId, int quantityRequested) {
        Optional<Product> product = productRepository.findById(productId);
        
        return product.filter(p -> p.getQuantitaDisponibile() >= quantityRequested).isPresent();
    }
    
    @Override
    public List<Product> findByCategoryName(String categoryName) {
        Optional<Category> category = categoryRepository.findByNome(categoryName);
        return category.map(productRepository::findByCategoria).orElse(List.of());
    }
    
    @Override
    public List<Product> findByCategoryName(String categoryName, int page, int size) {
        Optional<Category> category = categoryRepository.findByNome(categoryName);
        if (category.isPresent()) {
            Pageable pageable = PageRequest.of(page, size);
            return productRepository.findByCategoria(category.get(), pageable).getContent();
        }
        return List.of();
    }
    
    @Override
    public List<Product> findFeaturedProducts(int limit) {
        // Implementazione semplice: prende gli ultimi prodotti aggiunti
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.findAll(pageable).getContent();
    }
    
    @Override
    public List<Product> findRecentProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.findAll(pageable).getContent();
    }
    
    @Override
    public long countProducts() {
        return productRepository.count();
    }
    
    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
    
    @Override
    public Optional<Category> findCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    @Override
    public Optional<Category> findCategoryByName(String name) {
        return categoryRepository.findByNome(name);
    }
    
    @Override
    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        
        if (category.isPresent()) {
            // Verifica che non ci siano prodotti associati
            if (!category.get().getProdotti().isEmpty()) {
                throw new RuntimeException("Impossibile eliminare la categoria perché ha prodotti associati");
            }
            
            categoryRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
    
    /**
     * per salvare  un'immagine nel filesystem e restituisce il nome del file
     * 
     * @param imageFile  file dell'immagine
     * @return restituisce il nome del file salvato
     */
    private String saveImage(MultipartFile imageFile) {
        try {
            // Crea la directory se non esiste
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Directory creata on-demand: " + uploadPath.toAbsolutePath());
            }
            // log di debugging
            System.out.println("Tentativo di salvare immagine. Nome originale: " + imageFile.getOriginalFilename());
            System.out.println("Directory di destinazione: " + uploadPath.toAbsolutePath());
            // per generare un nome univoco per il file
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID() + extension;
            
            // per salvare  il file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Immagine salvata con successo: " + filePath.toAbsolutePath());
            return filename;
        } catch (IOException e) {
            System.err.println("Errore nel salvare l'immagine: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Errore nel salvare l'immagine: " + e.getMessage());
        }
    }
    
    /**
     * per eliminare un'immagine dal filesystem
     * 
     * @param imageName nome del file da eliminare
     */
    private void deleteImage(String imageName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(imageName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log dell'errore, ma non interrompe l'operazione
            System.err.println("Errore nell'eliminare l'immagine: " + e.getMessage());
        }
    }
}
