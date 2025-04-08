package com.tobysgift.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Cart;
import com.tobysgift.model.Order;
import com.tobysgift.model.User;
import com.tobysgift.service.CartService;
import com.tobysgift.service.OrderService;
import com.tobysgift.service.UserService;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    
    @Autowired
    public OrderController(OrderService orderService, 
                           UserService userService,
                           CartService cartService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
    }
    
    /**
     * Mostra il riepilogo del carrello prima della conferma dell'ordine
     */
    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String checkout(Model model, Principal principal) {
        // Otteniamo l'utente autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = principal.getName();
        
        logger.info("Checkout richiesto dall'utente: {}", email);
        
        User user = userService.findByEmail(email);
        if (user == null) {
            logger.error("Utente non trovato per username: {}", email);
            return "redirect:/login";
        }
        
        logger.info("Utente recuperato con ID: {}", user.getId());
        
        Cart cart = cartService.getCartByUser(user);

        
        // Verifica che il carrello non sia vuoto
        if (cart.getItems().isEmpty()) {
            logger.info("Carrello vuoto per l'utente: {}", email);
            return "redirect:/cart";
        }
        
        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        return "orders/checkout";
    }
    
    /**
     * Crea un nuovo ordine
     */
    @PostMapping("/place")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String placeOrder(@RequestParam("indirizzoConsegna") String indirizzoConsegna,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            // Otteniamo l'utente autenticato
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = principal.getName();
            
            logger.info("Creazione ordine richiesta dall'utente: {}", email);
            
            User user = userService.findByEmail(email);
            if (user == null) {
                logger.error("Utente non trovato per username: {}", email);
                redirectAttributes.addFlashAttribute("errorMessage", "Utente non trovato");
                return "redirect:/orders/checkout";
            }
            
            logger.info("Utente recuperato con ID: {}", user.getId());
            
            // Verifica che l'utente abbia un indirizzo
            if (indirizzoConsegna == null || indirizzoConsegna.trim().isEmpty()) {
                if (user.getIndirizzo() == null || user.getIndirizzo().trim().isEmpty()) {
                    logger.error("Indirizzo di consegna non specificato per l'utente: {}", email);
                    redirectAttributes.addFlashAttribute("errorMessage", 
                            "È necessario specificare un indirizzo di consegna");
                    return "redirect:/orders/checkout";
                }
                indirizzoConsegna = user.getIndirizzo();
            }
            
            // Crea l'ordine dal carrello
            Order newOrder = orderService.createOrderFromCart(user, indirizzoConsegna);
            
            logger.info("Ordine creato con ID: {}", newOrder.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Ordine creato con successo! Il tuo numero d'ordine è: " + newOrder.getId());
            
            return "redirect:/orders/" + newOrder.getId();
        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'ordine", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Si è verificato un errore: " + e.getMessage());
            return "redirect:/orders/checkout";
        }
    }
    
    /**
     * Mostra i dettagli di un ordine specifico
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String viewOrder(@PathVariable("id") Long id, 
                           Model model, 
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            logger.info("Visualizzazione ordine {} richiesta dall'utente: {}", id, username);
            
            User user = userService.findByUsername(username);
            if (user == null) {
                logger.error("Utente non trovato per username: {}", username);
                redirectAttributes.addFlashAttribute("errorMessage", "Utente non trovato");
                return "redirect:/orders";
            }
            
            // Controlla che l'utente sia autorizzato a visualizzare l'ordine
            if (!orderService.isUserAuthorized(id, user)) {
                logger.warn("Utente {} non autorizzato a visualizzare l'ordine {}", username, id);
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Non sei autorizzato a visualizzare questo ordine");
                return "redirect:/orders";
            }
            
            // Ottiene l'ordine
            Order order = orderService.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Ordine {} non trovato", id);
                        return new RuntimeException("Ordine non trovato");
                    });
            
            model.addAttribute("order", order);
            
            return "orders/view";
        } catch (Exception e) {
            logger.error("Errore durante la visualizzazione dell'ordine {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Si è verificato un errore: " + e.getMessage());
            return "redirect:/orders";
        }
    }
    
    /**
     * Mostra la lista degli ordini dell'utente
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listOrders(Model model, 
                            Principal principal,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            logger.info("Visualizzazione lista ordini richiesta dall'utente: {}", username);
            
            User user = userService.findByUsername(username);
            if (user == null) {
                logger.error("Utente non trovato per username: {}", username);
                redirectAttributes.addFlashAttribute("errorMessage", "Utente non trovato");
                return "redirect:/";
            }
            
            // Crea la paginazione con ordinamento per data (dal più recente)
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataOrdine"));
            
            // Ottiene gli ordini paginati
            Page<Order> ordersPage = orderService.findOrdersByUser(user, pageable);
            
            model.addAttribute("ordersPage", ordersPage);
            
            return "orders/list";
        } catch (Exception e) {
            logger.error("Errore durante la visualizzazione della lista ordini", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Si è verificato un errore: " + e.getMessage());
            return "redirect:/";
        }
    }
}