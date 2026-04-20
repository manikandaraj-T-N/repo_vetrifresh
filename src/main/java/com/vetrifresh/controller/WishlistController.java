package com.vetrifresh.controller;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vetrifresh.model.Product;
import com.vetrifresh.model.User;
import com.vetrifresh.model.Wishlist;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.repository.UserRepository;
import com.vetrifresh.repository.WishlistRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("wishlistItems", wishlistRepository.findByUser(user));
        return "wishlist";
    }

    @PostMapping("/add")
    public String addToWishlist(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam Long productId,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!wishlistRepository.existsByUserAndProduct(user, product)) {
            wishlistRepository.save(Wishlist.builder()
                    .user(user).product(product).build());
            redirectAttributes.addFlashAttribute("successMessage", "Added to wishlist!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Already in wishlist!");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/remove/{id}")
    public String removeFromWishlist(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        wishlistRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Removed from wishlist!");
        return "redirect:/wishlist";
    }

    @ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}
}