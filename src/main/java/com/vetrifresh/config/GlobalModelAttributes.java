package com.vetrifresh.config;

import com.vetrifresh.model.User;
import com.vetrifresh.repository.UserRepository;
import com.vetrifresh.repository.WishlistRepository;
import com.vetrifresh.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final CartService cartService;
    private final WishlistRepository wishlistRepository; // ← add this
    private final UserRepository userRepository;         // ← add this

    @ModelAttribute
    public void addGlobalAttributes(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (userDetails != null) {
            try {
                String email = userDetails.getUsername();

                // Cart count
                int cartCount = cartService.getCartCount(email);
                model.addAttribute("cartCount", cartCount);

                // Wishlist count ← add this block
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    int wishlistCount = wishlistRepository.countByUser(user);
                    model.addAttribute("wishlistCount", wishlistCount);
                } else {
                    model.addAttribute("wishlistCount", 0);
                }

            } catch (Exception e) {
                System.err.println("GlobalModelAttributes ERROR: " + e.getMessage());
                model.addAttribute("cartCount", 0);
                model.addAttribute("wishlistCount", 0);
            }
        } else {
            model.addAttribute("cartCount", 0);
            model.addAttribute("wishlistCount", 0);
        }
    }
}