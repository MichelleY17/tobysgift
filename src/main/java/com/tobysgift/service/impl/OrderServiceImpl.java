package com.tobysgift.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobysgift.exception.InsufficientQuantityException;
import com.tobysgift.model.Cart;
import com.tobysgift.model.Order;
import com.tobysgift.model.Product;
import com.tobysgift.model.Role;
import com.tobysgift.model.User;
import com.tobysgift.repository.OrderRepository;
import com.tobysgift.service.CartService;
import com.tobysgift.service.OrderService;
import com.tobysgift.service.ProductService;

@Service
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                           CartService cartService,
                           ProductService productService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
    }
    
    @Override
    @Transactional
    public Order createOrderFromCart(User user, String indirizzoConsegna) {
        if (user == null) {
            throw new IllegalArgumentException("L'utente non può essere null");
        }
        
        // Ottiene il carrello dell'utente
        Cart cart = cartService.getCartByUser(user);
        
        // Verifica che il carrello non sia vuoto
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Il carrello è vuoto");
        }
        
        // Verifica la disponibilità dei prodotti
        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            int quantityRequested = cartItem.getQuantita();
            
            if (product.getQuantitaDisponibile() < quantityRequested) {
                throw new InsufficientQuantityException(
                        "Quantità non disponibile per il prodotto: " + product.getNome());
            }
        });
        
        // Crea ordine dal carrello
        Order order = Order.creaOrdineFromCarrello(cart, indirizzoConsegna);
        
        // Aggiorna le quantità dei prodotti
        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            product.setQuantitaDisponibile(product.getQuantitaDisponibile() - cartItem.getQuantita());
            productService.update(product, null);
        });
        
        // Salva l'ordine
        Order savedOrder = orderRepository.save(order);
        
        // Svuota il carrello dopo aver creato l'ordine
        cartService.clearCart(user);
        
        return savedOrder;
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    @Override
    public List<Order> findOrdersByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utente non può essere null");
        }
        return orderRepository.findByUserOrderByDataOrdineDesc(user);
    }
    
    @Override
    public Page<Order> findOrdersByUser(User user, Pageable pageable) {
        if (user == null) {
            throw new IllegalArgumentException("L'utente non può essere null");
        }
        return orderRepository.findByUser(user, pageable);
    }
    
    @Override
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    @Override
    public boolean isUserAuthorized(Long orderId, User user) {
        if (user == null) {
            return false;
        }
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            return false;
        }
        
        Order order = orderOpt.get();
        
        // Utente è autorizzato se è il proprietario dell'ordine o è un admin
        return order.getUser().getId().equals(user.getId()) || user.hasRole(Role.ADMIN);
    }
    
    @Override
    @Transactional
    public Order saveOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("L'ordine non può essere null");
        }
        
        return orderRepository.save(order);
    }
}