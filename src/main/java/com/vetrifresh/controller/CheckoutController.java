package com.vetrifresh.controller;

import com.vetrifresh.model.*;
import com.vetrifresh.repository.OrderRepository;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.repository.UserRepository;
import com.vetrifresh.service.CartService;
import com.vetrifresh.service.EmailService;
import com.vetrifresh.service.RazorpayService;

import lombok.RequiredArgsConstructor;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService       cartService;
    private final OrderRepository   orderRepository;
    private final UserRepository    userRepository;
     private final ProductRepository productRepository;
      private final EmailService      emailService;  
    private final RazorpayService   razorpayService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @GetMapping("/checkout")
    public String checkoutPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (userDetails == null) return "redirect:/login";

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartService.getCartItems(email);
        if (cartItems.isEmpty()) return "redirect:/cart";

        BigDecimal subtotal = cartItems.stream()
                .map(ci -> ci.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal delivery = subtotal.compareTo(new BigDecimal("500")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("40");

        model.addAttribute("cartItems",    cartItems);
        model.addAttribute("user",         user);
        model.addAttribute("subtotal",     subtotal);
        model.addAttribute("delivery",     delivery);
        model.addAttribute("total",        subtotal.add(delivery));
        model.addAttribute("razorpayKeyId", razorpayKeyId);

        return "checkout";
    }

    @PostMapping("/checkout")
    @Transactional
    public String processCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam BigDecimal subtotal,
            @RequestParam BigDecimal deliveryCharge,
            @RequestParam BigDecimal total,
            @RequestParam String shippingName,
            @RequestParam String shippingPhone,
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingPincode,
            @RequestParam String paymentMethod,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<CartItem> cartItems = cartService.getCartItems(email);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty!");
                return "redirect:/cart";
            }

            // Build and save order
            Order order = Order.builder()
                    .user(user)
                    .totalAmount(total)
                    .deliveryCharge(deliveryCharge)
                    .discountAmount(BigDecimal.ZERO)
                    .status(Order.OrderStatus.PENDING)
                    .paymentStatus(Order.PaymentStatus.PENDING)
                    .paymentMethod(paymentMethod)
                    .shippingName(shippingName)
                    .shippingAddress(shippingAddress)
                    .shippingCity(shippingCity)
                    .shippingPincode(shippingPincode)
                    .shippingPhone(shippingPhone)
                    .build();

            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem ci : cartItems) {
                BigDecimal unitPrice  = ci.getProduct().getPrice();
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
                orderItems.add(OrderItem.builder()
                        .order(order)
                        .product(ci.getProduct())
                        .quantity(ci.getQuantity())
                        .unitPrice(unitPrice)
                        .totalPrice(totalPrice)
                        .build());
            }
            order.setOrderItems(orderItems);
            orderRepository.save(order);

            // After orderRepository.save(order)
try {
    emailService.sendOrderConfirmation(user.getEmail(), order);
} catch (Exception e) {
    // Don't fail the order if email fails
    System.err.println("Email failed: " + e.getMessage());
}

            // After building orderItems, reduce stock
for (CartItem ci : cartItems) {
    Product product = ci.getProduct();
    int newStock = product.getStockQuantity() - ci.getQuantity();
    product.setStockQuantity(Math.max(0, newStock)); // never go below 0
    productRepository.save(product);
}

            // COD — clear cart and redirect to success
            if ("COD".equals(paymentMethod)) {
                cartService.clearCart(email);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Order placed! Order ID: #" + order.getOrderNumber());
                return "redirect:/order/success/" + order.getId();
            }

            // UPI/Online — create Razorpay order and show payment page
            JSONObject razorpayOrder = razorpayService.createOrder(
                    total, order.getOrderNumber());

            model.addAttribute("order",           order);
            model.addAttribute("razorpayOrderId", razorpayOrder.getString("id"));
            model.addAttribute("razorpayKeyId",   razorpayKeyId);
            model.addAttribute("amount",          total.multiply(new BigDecimal("100")).intValue());
            model.addAttribute("userEmail",       user.getEmail());
            model.addAttribute("userName",        shippingName);
            model.addAttribute("userPhone",       shippingPhone);

            return "payment";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Order failed: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    // Called after Razorpay payment success
    @PostMapping("/payment/verify")
    @Transactional
    public String verifyPayment(
            @RequestParam String razorpay_order_id,
            @RequestParam String razorpay_payment_id,
            @RequestParam String razorpay_signature,
            @RequestParam Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        boolean valid = razorpayService.verifySignature(
                razorpay_order_id, razorpay_payment_id, razorpay_signature);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (valid) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            cartService.clearCart(userDetails.getUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment successful! Order #" + order.getOrderNumber());
            return "redirect:/order/success/" + order.getId();

        } else {
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Payment verification failed. Please contact support.");
            return "redirect:/cart";
        }
    }

    @GetMapping("/order/success/{id}")
    @Transactional
    public String orderSuccess(@PathVariable Long id, Model model) {
        orderRepository.findById(id).ifPresent(order -> {
            order.getOrderItems().size(); // force lazy load
            model.addAttribute("order", order);
        });
        return "order-success";
    }


    @PostMapping("/checkout/initiate")
@Transactional
@ResponseBody
public Map<String, Object> initiatePayment(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam BigDecimal subtotal,
        @RequestParam BigDecimal deliveryCharge,
        @RequestParam BigDecimal total,
        @RequestParam String shippingName,
        @RequestParam String shippingPhone,
        @RequestParam String shippingAddress,
        @RequestParam String shippingCity,
        @RequestParam String shippingPincode,
        @RequestParam String paymentMethod) {

    try {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartService.getCartItems(email);
        if (cartItems.isEmpty()) {
            return Map.of("error", "Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .deliveryCharge(deliveryCharge)
                .discountAmount(BigDecimal.ZERO)
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .shippingName(shippingName)
                .shippingAddress(shippingAddress)
                .shippingCity(shippingCity)
                .shippingPincode(shippingPincode)
                .shippingPhone(shippingPhone)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            BigDecimal unitPrice  = ci.getProduct().getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build());
        }
        order.setOrderItems(orderItems);
        orderRepository.save(order);

        JSONObject rzpOrder = razorpayService.createOrder(total, order.getOrderNumber());

        return Map.of(
            "orderId",         order.getId(),
            "orderNumber",     order.getOrderNumber(),
            "razorpayOrderId", rzpOrder.getString("id"),
            "amount",          total.multiply(new BigDecimal("100")).intValue()
        );

    } catch (Exception e) {
        return Map.of("error", e.getMessage());
    }
}

}