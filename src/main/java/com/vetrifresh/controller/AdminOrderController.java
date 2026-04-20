package com.vetrifresh.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vetrifresh.model.Order;
import com.vetrifresh.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

 @Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    @Transactional
    public String listOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        orders.forEach(o -> o.getOrderItems().size());
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/orders";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam Order.OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("successMessage", "Status updated!");
        return "redirect:/admin/orders";
    }
} 
