package com.tobysgift.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tobysgift.model.Order;
import com.tobysgift.model.OrderStatus;
import com.tobysgift.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminOrderController {
    
    private final OrderService orderService;
    
    @Autowired
    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Mostra la lista di tutti gli ordini
     */
    @GetMapping
    public String listOrders(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(defaultValue = "dataOrdine,desc") String sort) {
        
        // Parsing del parametro di ordinamento
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
                                      Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Creazione dell'oggetto Pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        // Recupero degli ordini
        Page<Order> ordersPage = orderService.findAllOrders(pageable);
        
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("sort", sort);
        model.addAttribute("orderStatuses", OrderStatus.values());
        
        return "admin/orders";
    }
    
    /**
     * Mostra i dettagli di un ordine specifico
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato"));
        
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        
        return "admin/view";
    }
    
    /**
     * Aggiorna lo stato di un ordine
     */
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable("id") Long id,
                                   @RequestParam("status") String statusStr,
                                   RedirectAttributes redirectAttributes) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr);
            
            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ordine non trovato"));
            
            // Verifica se lo stato è cambiato
            if (order.getStatus() != newStatus) {
                order.setStatus(newStatus);
                orderService.saveOrder(order);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Stato dell'ordine aggiornato a: " + newStatus.getDescrizione());
            }
            
            return "redirect:/admin/orders/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Stato dell'ordine non valido: " + statusStr);
            return "redirect:/admin/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Errore nell'aggiornare lo stato dell'ordine: " + e.getMessage());
            return "redirect:/admin/orders/" + id;
        }
    }
}
