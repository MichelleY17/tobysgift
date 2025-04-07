package com.tobysgift.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tobysgift.model.Category;
import com.tobysgift.model.Product;
import com.tobysgift.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     *per mostrare la lista dei prodotti con paginazione, ordinamento e filtri
     */
    @GetMapping
    public String listProducts(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "nome,asc") String sort) {
        
        // Parsing del parametro di ordinamento
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
                                       Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Creazione dell'oggetto Pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        // Recupero dei prodotti in base ai parametri
        Page<Product> productPage;
        
        if (search != null && !search.isEmpty() && categoryId != null) {
            // Ricerca per parola chiave e categoria
            productPage = productService.searchProductsByCategory(search, categoryId, pageable);
            model.addAttribute("categoryId", categoryId);
        } else if (search != null && !search.isEmpty()) {
            // Ricerca per parola chiave
            productPage = productService.searchProducts(search, pageable);
        } else if (categoryId != null) {
            // Filtro per categoria
            productPage = productService.findByCategory(categoryId, pageable);
            model.addAttribute("categoryId", categoryId);
            
            // Aggiungi il nome della categoria al modello se disponibile
            productService.findCategoryById(categoryId).ifPresent(category -> 
                model.addAttribute("categoryName", category.getNome()));
        } else {
            // Nessun filtro
            productPage = productService.findAll(pageable);
        }
        
        // Caricamento delle categorie per i filtri
        List<Category> categories = productService.findAllCategories();
        
        // Preparazione del modello
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        
        return "products/list";
    }
    
    /**
     * Mostra i dettagli di un prodotto
     */
    @GetMapping("/{id}")
    public String showProduct(@PathVariable("id") Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            
            // Recupera prodotti correlati (stessa categoria)
            List<Product> relatedProducts = productService.findByCategoryName(
                    product.get().getCategoria().getNome(), 0, 4);
            
            // Filtra il prodotto corrente dalla lista dei correlati
            relatedProducts = relatedProducts.stream()
                    .filter(p -> !p.getId().equals(id))
                    .limit(3)
                    .toList();
                    
            model.addAttribute("relatedProducts", relatedProducts);
            
            return "products/detail";
        } else {
            return "redirect:/products";
        }
    }
    
    /**
     * Mostra i prodotti di una categoria
     */
    @GetMapping("/category/{name}")
    public String productsByCategory(
            @PathVariable("name") String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        Optional<Category> category = productService.findCategoryByName(categoryName);
        
        if (category.isPresent()) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nome"));
            Page<Product> productPage = productService.findByCategory(category.get().getId(), pageable);
            
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("page", productPage);
            model.addAttribute("category", category.get());
            model.addAttribute("categories", productService.findAllCategories());
            
            return "products/list";
        } else {
            return "redirect:/products";
        }
    }
}
