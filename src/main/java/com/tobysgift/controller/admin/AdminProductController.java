package com.tobysgift.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Category;
import com.tobysgift.model.Product;
import com.tobysgift.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    
    private final ProductService productService;
    
    @Autowired
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * per mostrare la lista dei prodotti per l'amministratore
     */
    @GetMapping
    public String listProducts(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
        
        if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            // Ricerca per parola chiave e categoria
            Optional<Category> cat = productService.findCategoryByName(category);
            if (cat.isPresent()) {
                productPage = productService.searchProductsByCategory(search, cat.get().getId(), pageable);
            } else {
                productPage = productService.searchProducts(search, pageable);
            }
        } else if (search != null && !search.isEmpty()) {
            // Ricerca per parola chiave
            productPage = productService.searchProducts(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            // Filtro per categoria
            Optional<Category> cat = productService.findCategoryByName(category);
            if (cat.isPresent()) {
                productPage = productService.findByCategory(cat.get().getId(), pageable);
            } else {
                productPage = productService.findAll(pageable);
            }
        } else {
            // Nessun filtro
            productPage = productService.findAll(pageable);
        }
        
        // Caricamento delle categorie per i filtri
        List<Category> categories = productService.findAllCategories();
        
        // Preparazione del modello
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        
        return "admin/products";
    }
    
    /**
     * per mostrare il form per creare un nuovo prodotto
     */
    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("mode", "create");
        
        return "admin/product-form";
    }
    
    /**
     * per gestire il salvataggio di un nuovo prodotto
     */
    @PostMapping("/new")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.findAllCategories());
            model.addAttribute("mode", "create");
            return "admin/product-form";
        }
        
        try {
            // Imposta la categoria
            Optional<Category> category = productService.findCategoryById(categoryId);
            if (category.isEmpty()) {
                bindingResult.rejectValue("categoria", "error.product", "Categoria non valida");
                model.addAttribute("categories", productService.findAllCategories());
                model.addAttribute("mode", "create");
                return "admin/product-form";
            }
            
            product.setCategoria(category.get());
            
            // Salva il prodotto
            productService.save(product, imageFile);
            
            redirectAttributes.addFlashAttribute("successMessage", "Prodotto creato con successo");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nel salvare il prodotto: " + e.getMessage());
            model.addAttribute("categories", productService.findAllCategories());
            model.addAttribute("mode", "create");
            return "admin/product-form";
        }
    }
    
    /**
     *per mostrare il form per modificare un prodotto esistente
     */
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("categories", productService.findAllCategories());
            model.addAttribute("mode", "edit");
            
            return "admin/product-form";
        } else {
            return "redirect:/admin/products";
        }
    }
    
    /**
     * per gestire l'aggiornamento di un prodotto esistente
     */
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!id.equals(product.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID prodotto non valido");
            return "redirect:/admin/products";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.findAllCategories());
            model.addAttribute("mode", "edit");
            return "admin/product-form";
        }
        
        try {
            // Imposta la categoria
            Optional<Category> category = productService.findCategoryById(categoryId);
            if (category.isEmpty()) {
                bindingResult.rejectValue("categoria", "error.product", "Categoria non valida");
                model.addAttribute("categories", productService.findAllCategories());
                model.addAttribute("mode", "edit");
                return "admin/product-form";
            }
            
            product.setCategoria(category.get());
            
            // Aggiorna il prodotto
            productService.update(product, imageFile);
            
            redirectAttributes.addFlashAttribute("successMessage", "Prodotto aggiornato con successo");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore nell'aggiornare il prodotto: " + e.getMessage());
            model.addAttribute("categories", productService.findAllCategories());
            model.addAttribute("mode", "edit");
            return "admin/product-form";
        }
    }
    
    /**
     * per eliminare un prodotto
     */
    @PostMapping("/delete")
    public String deleteProduct(
            @RequestParam("productId") Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean deleted = productService.delete(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Prodotto eliminato con successo");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Prodotto non trovato");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nell'eliminare il prodotto: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * per aggiornare la quantità di un prodotto
     */
    @PostMapping("/{id}/update-quantity")
    public String updateProductQuantity(
            @PathVariable("id") Long id,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (quantity < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "La quantità non può essere negativa");
                return "redirect:/admin/products";
            }
            
            Product updatedProduct = productService.updateQuantity(id, quantity);
            
            if (updatedProduct != null) {
                redirectAttributes.addFlashAttribute("successMessage", "Quantità aggiornata con successo");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Prodotto non trovato");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nell'aggiornare la quantità: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * per mostrare il form per gestire le categorie
     */
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", productService.findAllCategories());
        model.addAttribute("newCategory", new Category());
        
        return "admin/categories";
    }
    
    /**
     * per creare  una nuova categoria
     */
    @PostMapping("/categories/new")
    public String createCategory(
            @Valid @ModelAttribute("newCategory") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.findAllCategories());
            return "admin/categories";
        }
        
        try {
            // per verificare  se esiste già una categoria con lo stesso nome
            if (productService.findCategoryByName(category.getNome()).isPresent()) {
                bindingResult.rejectValue("nome", "error.category", "Esiste già una categoria con questo nome");
                model.addAttribute("categories", productService.findAllCategories());
                return "admin/categories";
            }
            
            // per salvare  la categoria
            productService.saveCategory(category);
            
            redirectAttributes.addFlashAttribute("successMessage", "Categoria creata con successo");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nel creare la categoria: " + e.getMessage());
        }
        
        return "redirect:/admin/products/categories";
    }
    
    /**
     * per eliminare una categoria
     */
    @PostMapping("/categories/delete")
    public String deleteCategory(
            @RequestParam("categoryId") Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean deleted = productService.deleteCategory(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Categoria eliminata con successo");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Categoria non trovata");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nell'eliminare la categoria: " + e.getMessage());
        }
        
        return "redirect:/admin/products/categories";
    }
}