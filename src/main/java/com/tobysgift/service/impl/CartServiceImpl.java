package com.tobysgift.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobysgift.exception.InsufficientQuantityException;
import com.tobysgift.model.Cart;
import com.tobysgift.model.CartItem;
import com.tobysgift.model.Product;
import com.tobysgift.model.User;
import com.tobysgift.repository.CartItemRepository;
import com.tobysgift.repository.CartRepository;
import com.tobysgift.repository.ProductRepository;
import com.tobysgift.service.CartService;
import com.tobysgift.service.ProductService;

/**
 * Implementazione del servizio per la gestione del carrello.
 */
@Service
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    
    @Autowired
    public CartServiceImpl(CartRepository cartRepository, 
                          CartItemRepository cartItemRepository,
                          ProductRepository productRepository,
                          ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }
    
    @Override
    @Transactional
    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    // Crea un nuovo carrello se non esiste
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }
    
    @Override
    @Transactional
    public CartItem addProductToCart(User user, Long productId, int quantity) {
        // Verifica la disponibilità del prodotto
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato"));
        
        if (product.getQuantitaDisponibile() < quantity) {
            throw new InsufficientQuantityException("Quantità richiesta non disponibile");
        }
        
        // Ottiene il carrello dell'utente
        Cart cart = getCartByUser(user);
        
        // Verifica se il prodotto è già nel carrello
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // Aggiorna la quantità dell'elemento esistente
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantita() + quantity;
            
            // Verifica ancora la disponibilità
            if (product.getQuantitaDisponibile() < newQuantity) {
                throw new InsufficientQuantityException("Quantità richiesta non disponibile");
            }
            
            item.setQuantita(newQuantity);
            CartItem updatedItem = cartItemRepository.save(item);
            
            // Ricalcola il totale del carrello
            updateCartTotal(cart);
            
            return updatedItem;
        } else {
            // Crea un nuovo elemento nel carrello
            CartItem newItem = new CartItem(cart, product, quantity);
            CartItem savedItem = cartItemRepository.save(newItem);
            
            // Aggiorna il carrello
            cart.getItems().add(savedItem);
            updateCartTotal(cart);
            
            return savedItem;
        }
    }
    
    @Override
    @Transactional
    public CartItem updateCartItemQuantity(User user, Long cartItemId, int quantity) {
        // Ottiene il carrello dell'utente
        Cart cart = getCartByUser(user);
        
        // Trova l'elemento del carrello
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Elemento del carrello non trovato"));
        
        // Verifica che l'elemento appartenga al carrello dell'utente
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("L'elemento non appartiene al carrello dell'utente");
        }
        
        // Verifica la disponibilità del prodotto
        Product product = cartItem.getProduct();
        if (product.getQuantitaDisponibile() < quantity) {
            throw new InsufficientQuantityException("Quantità richiesta non disponibile");
        }
        
        // Aggiorna la quantità
        cartItem.setQuantita(quantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);
        
        // Ricalcola il totale del carrello
        updateCartTotal(cart);
        
        return updatedItem;
    }
    
    @Override
    @Transactional
    public boolean removeCartItem(User user, Long cartItemId) {
        // Ottiene il carrello dell'utente
        Cart cart = getCartByUser(user);
        
        // Trova l'elemento del carrello
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        
        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            
            // Verifica che l'elemento appartenga al carrello dell'utente
            if (cartItem.getCart().getId().equals(cart.getId())) {
                // Rimuove l'elemento dal carrello
                cartItemRepository.delete(cartItem);
                cart.getItems().remove(cartItem);
                
                // Ricalcola il totale del carrello
                updateCartTotal(cart);
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public void clearCart(User user) {
        // Ottiene il carrello dell'utente
        Cart cart = getCartByUser(user);
        
        // Rimuove tutti gli elementi del carrello
        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
        
        // Aggiorna il totale del carrello
        cart.setTotale(BigDecimal.ZERO);
        cartRepository.save(cart);
    }
    
    @Override
    public int getCartItemCount(User user) {
        // Ottiene il carrello dell'utente
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        
        if (cartOpt.isPresent()) {
            return cartOpt.get().getTotalProductCount();
        }
        
        return 0;
    }
    
    @Override
    public BigDecimal getCartItemTotal(Long cartItemId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        
        return cartItemOpt.map(CartItem::getPrezzoTotale).orElse(BigDecimal.ZERO);
    }
    
    @Override
    public boolean isProductInCart(User user, Long productId) {
        // Ottiene il carrello dell'utente
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // Verifica se il prodotto è nel carrello
            return cart.getItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(productId));
        }
        
        return false;
    }
    
    @Override
    public CartItem getCartItemByProduct(User user, Long productId) {
        // Ottiene il carrello dell'utente
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // Trova l'elemento del carrello per il prodotto specificato
            return cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElse(null);
        }
        
        return null;
    }
    
    @Override
    public List<Product> getRelatedProducts(User user, int limit) {
        // Ottiene il carrello dell'utente
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        
        if (cartOpt.isPresent() && !cartOpt.get().getItems().isEmpty()) {
            Cart cart = cartOpt.get();
            
            // Ottiene le categorie dei prodotti nel carrello
            List<Long> categoryIds = cart.getItems().stream()
                    .map(item -> item.getProduct().getCategoria().getId())
                    .distinct()
                    .collect(Collectors.toList());
            
            // Ottiene i prodotti correlati basati sulle stesse categorie
            List<Product> relatedProducts = new ArrayList<>();
            
            for (Long categoryId : categoryIds) {
                // Ottiene prodotti dalla stessa categoria, limitati per ogni categoria
                Pageable pageable = PageRequest.of(0, limit / categoryIds.size() + 1);
                List<Product> productsInCategory = productService.findByCategory(categoryId, pageable).getContent();
                
                // Filtra i prodotti che sono già nel carrello
                List<Long> productIdsInCart = cart.getItems().stream()
                        .map(item -> item.getProduct().getId())
                        .collect(Collectors.toList());
                
                List<Product> filteredProducts = productsInCategory.stream()
                        .filter(product -> !productIdsInCart.contains(product.getId()))
                        .collect(Collectors.toList());
                
                relatedProducts.addAll(filteredProducts);
            }
            
            // Limita il numero totale di prodotti correlati
            return relatedProducts.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        // Se il carrello è vuoto, restituisce i prodotti in evidenza
        return productService.findFeaturedProducts(limit);
    }
    
    /**
     * Aggiorna il totale del carrello.
     * 
     * @param cart Il carrello da aggiornare
     */
    private void updateCartTotal(Cart cart) {
        cart.calcolaTotale();
        cartRepository.save(cart);
    }
}