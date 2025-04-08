package com.tobysgift.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Cart;
import com.tobysgift.model.Product;
import com.tobysgift.model.User;
import com.tobysgift.service.CartService;
import com.tobysgift.service.ProductService;
import com.tobysgift.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    
    @Autowired
    public CartController(CartService cartService, UserService userService, ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
    }
    
    /**
     * per visualizzare il contenuto del carrello
     */
    @GetMapping
    public String viewCart(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userService.findByEmail(auth.getName());
            if (user != null) {
                Cart cart = cartService.getCartByUser(user);
                model.addAttribute("cart", cart);
                
                // per aggiungere prodotti consigliati
                List<Product> recommendedProducts = getRecommendedProducts(cart, 3);
                model.addAttribute("recommendedProducts", recommendedProducts);
                
                return "cart";
            }
        }
        // se l'uente non è loggato rindirizza al form login
        return "redirect:/login";
    }
    
    /**
     * per aggiungere un prodotto al carrello
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId, 
                                      @RequestParam(defaultValue = "1") int quantity) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                User user = userService.findByEmail(auth.getName());
                cartService.addProductToCart(user, productId, quantity);
                
                response.put("success", true);
                response.put("message", "Prodotto aggiunto al carrello");
                response.put("cartItemCount", cartService.getCartItemCount(user));
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "È necessario effettuare il login per aggiungere prodotti al carrello");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Si è verificato un errore: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * per aggiorare la quantita di un prodotto nel carrello
     */
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> updateCartItemQuantity(@PathVariable Long cartItemId, 
                                                  @RequestParam int quantity) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                User user = userService.findByEmail(auth.getName());
                cartService.updateCartItemQuantity(user, cartItemId, quantity);
                
                Cart cart = cartService.getCartByUser(user);
                
                response.put("success", true);
                response.put("message", "Quantità aggiornata");
                response.put("cartTotal", cart.getTotale());
                response.put("itemTotal", cartService.getCartItemTotal(cartItemId));
                response.put("cartItemCount", cartService.getCartItemCount(user));
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Utente non autenticato");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Si è verificato un errore: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * per rimjuovere un prodott dal carrello
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long cartItemId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                User user = userService.findByEmail(auth.getName());
                cartService.removeCartItem(user, cartItemId);
                
                Cart cart = cartService.getCartByUser(user);
                
                response.put("success", true);
                response.put("message", "Prodotto rimosso dal carrello");
                response.put("cartTotal", cart.getTotale());
                response.put("cartItemCount", cartService.getCartItemCount(user));
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Utente non autenticato");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Si è verificato un errore: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * per svuotare il carrello
     */
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                User user = userService.findByEmail(auth.getName());
                cartService.clearCart(user);
                
                redirectAttributes.addFlashAttribute("successMessage", "Carrello svuotato");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Si è verificato un errore: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    /**
     * per rendirizzare alcheckout quando si procede al ordine
     */
    @PostMapping("/checkout")
    public String checkout(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userService.findByEmail(auth.getName());
            Cart cart = cartService.getCartByUser(user);
            
            if (cart.getItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Il carrello è vuoto");
                return "redirect:/cart";
            }
            
            return "redirect:/orders/checkout";
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", "È necessario effettuare il login per procedere al checkout");
        return "redirect:/login";
    }
    
    /**
     * èr ottener eprodotti consigliati in base al contenuto del carrello
     */
    private List<Product> getRecommendedProducts(Cart cart, int limit) {
        // per ottenere la categoria dei prodotti nel csrello
        List<Long> cartProductIds = cart.getItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());
        
        List<Long> cartCategoryIds = cart.getItems().stream()
                .map(item -> item.getProduct().getCategoria().getId())
                .distinct()
                .collect(Collectors.toList());
        
        // se non ci sono prodotti si restituisce prodotti in evidenza
        if (cartCategoryIds.isEmpty()) {
            return productService.findFeaturedProducts(limit);
        }
        
        //per trovare prodotti nella stessa cTEGORIA ESCLUDENDO QUELLI nel carrello
        return productService.findAll().stream()
                .filter(p -> cartCategoryIds.contains(p.getCategoria().getId()))
                .filter(p -> !cartProductIds.contains(p.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    //per mantenere il conteggio degli articoli del carrello nella sessione utente
    @ModelAttribute
    public void addCartItemCountToSession(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userService.findByEmail(auth.getName());
            if (user != null) {
                int cartItemCount = cartService.getCartItemCount(user);
                session.setAttribute("cartItemCount", cartItemCount);
            }
        }
    }
}