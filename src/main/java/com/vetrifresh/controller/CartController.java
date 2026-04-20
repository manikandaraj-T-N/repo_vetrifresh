package com.vetrifresh.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vetrifresh.repository.UserRepository;


import com.vetrifresh.model.CartItem;
import com.vetrifresh.model.User;
import com.vetrifresh.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
        private final UserRepository userRepository;

    @GetMapping
    public String viewCart(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();

        List<CartItem> items = cartService.getCartItems(email);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartService.getCartTotal(email));
      

        return "cart"; 
    }

    // POST /cart/add
    @PostMapping("/add")
public String addToCart(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long productId,
        @RequestParam(defaultValue = "1") int quantity,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes) {

    // If user not logged in → redirect to login
    if (userDetails == null) {
        return "redirect:/login";
    }

    try {
        cartService.addToCart(userDetails.getUsername(), productId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Item added to cart!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Could not add item: " + e.getMessage());
    }

    // Go back to whatever page the user was on
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/");
}


    // POST /cart/update/{itemId}
    @PostMapping("/update/{itemId}")
    public String updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.updateQuantity(userDetails.getUsername(), itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    // POST /cart/remove/{itemId}
    @PostMapping("/remove/{itemId}")
    public String removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.removeItem(userDetails.getUsername(), itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    // POST /cart/clear
    @PostMapping("/clear")
    public String clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.clearCart(userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Cart cleared!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to clear cart: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    @ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}
}