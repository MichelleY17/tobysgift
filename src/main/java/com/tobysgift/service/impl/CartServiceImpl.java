package com.tobysgift.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobysgift.model.Cart;
import com.tobysgift.model.CartItem;
import com.tobysgift.model.Product;
import com.tobysgift.model.User;
import com.tobysgift.repository.CartItemRepository;
import com.tobysgift.repository.CartRepository;
import com.tobysgift.repository.ProductRepository;
import com.tobysgift.service.CartService;

@Service
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    @Autowired
    public CartServiceImpl(CartRepository cartRepository, 
                          CartItemRepository cartItemRepository, 
                          ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }
    
    @Override
    @Transactional
    public Cart getCartByUser(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            // per creare un nuovo carrello per l'utente
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        }
    }
    
    @Override
    @Transactional
    public CartItem addProductToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di zero");
        }
        
        // per ottenere il prodotto
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato"));
        
        // per verificare la disponibilità
        if (product.getQuantitaDisponibile() < quantity) {
            throw new RuntimeException("Quantità richiesta non disponibile");
        }
        
        // per ottenere il carrello
        Cart cart = getCartByUser(user);
        
        // per verificare se il prodotto è già nel carrello
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // per aggiornare  la quantità
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantita() + quantity;
            
            // per verificare che la nuova quantità non superi la disponibilità
            if (newQuantity > product.getQuantitaDisponibile()) {
                throw new RuntimeException("Quantità richiesta non disponibile");
            }
            
            item.setQuantita(newQuantity);
            cartItemRepository.save(item);
            
            // per aggiornare il totale del carrello
            cart.calcolaTotale();
            cartRepository.save(cart);
            
            return item;
        } else {
            // per creare un nuovo elemento nel carrello
            CartItem newItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(newItem);
            
            // per aggiornare il carrello
            cart.getItems().add(newItem);
            cart.calcolaTotale();
            cartRepository.save(cart);
            
            return newItem;
        }
    }
    
    @Override
    @Transactional
    public CartItem updateCartItemQuantity(User user, Long cartItemId, int quantity) {
        // per verificare se la quantita nn è valida
        if (quantity < 0) {
            throw new IllegalArgumentException("La quantità non può essere negativa");
        }
        
        // se la quantità è zero, l'elemento viene rimosso
        if (quantity == 0) {
            removeCartItem(user, cartItemId);
            return null;
        }
        
        // per ottenere il carrello
        Cart cart = getCartByUser(user);
        
        // per ottenere l'elemento del carrello
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Elemento del carrello non trovato"));
        
        // per verificare che l'elemento appartenga al carrello dell'utente
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("L'elemento non appartiene al carrello dell'utente");
        }
        
        // per verificare  la disponibilità
        if (cartItem.getProduct().getQuantitaDisponibile() < quantity) {
            throw new RuntimeException("Quantità richiesta non disponibile");
        }
        
        // per aggiornare la quantità
        cartItem.setQuantita(quantity);
        cartItemRepository.save(cartItem);
        
        // per aggiornare  il totale del carrello
        cart.calcolaTotale();
        cartRepository.save(cart);
        
        return cartItem;
    }
    
    @Override
    @Transactional
    public void removeCartItem(User user, Long cartItemId) {
        // per ottenere il carrello
        Cart cart = getCartByUser(user);
        
        // per ottenere  l'elemento del carrello
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Elemento del carrello non trovato"));
        
        // per verificare che l'elemento appartenga al carrello dell'utente
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("L'elemento non appartiene al carrello dell'utente");
        }
        
        // per rimuovere l'elemento dal carrello
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        // infine per aggiornare il totale del carrello
        cart.calcolaTotale();
        cartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        
        // per rimuovere tutti gli elementi dal carrello
        cartItemRepository.deleteByCart(cart);
        
        // per azzerare il totale del carrello
        cart.getItems().clear();
        cart.setTotale(BigDecimal.ZERO);
        cartRepository.save(cart);
    }
    
    @Override
    public BigDecimal calculateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : cart.getItems()) {
            total = total.add(item.getPrezzoTotale());
        }
        
        return total;
    }
    
    @Override
    public int getCartItemCount(User user) {
        Cart cart = getCartByUser(user);
        return cart.getItems().size();
    }
    
    @Override
    public BigDecimal getCartItemTotal(Long cartItemId) {
        Optional<CartItem> cartItem = cartItemRepository.findById(cartItemId);
        return cartItem.map(CartItem::getPrezzoTotale).orElse(BigDecimal.ZERO);
    }
    
    @Override
    public boolean isProductInCart(User user, Long productId) {
        Cart cart = getCartByUser(user);
        
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                return true;
            }
        }
        
        return false;
    }
}