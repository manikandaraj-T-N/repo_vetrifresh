package com.vetrifresh.controller;

import com.vetrifresh.model.Order;
import com.vetrifresh.model.User;
import com.vetrifresh.repository.OrderRepository;
import com.vetrifresh.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

@Controller                     
@RequestMapping("/orders")      
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;


    @GetMapping
    @Transactional
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        orders.forEach(o -> o.getOrderItems().size()); // force lazy load

        model.addAttribute("orders", orders);
        return "my-orders";      // → templates/my-orders.html
    }

    // GET /orders/{id}  →  order-detail.html
    @GetMapping("/{id}")
    @Transactional
    public String orderDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (userDetails == null) return "redirect:/login";

        Order order = orderRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.getOrderItems().size(); // force lazy load
        model.addAttribute("order", order);
        return "order-detail";   // → templates/order-detail.html
    }

    @ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}
}