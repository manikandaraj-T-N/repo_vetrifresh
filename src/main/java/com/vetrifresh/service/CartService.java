package com.vetrifresh.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vetrifresh.model.CartItem;
import com.vetrifresh.model.Product;
import com.vetrifresh.model.User;
import com.vetrifresh.repository.CartRepository;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Single helper — avoid repeating this everywhere
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public List<CartItem> getCartItems(String email) {
        return cartRepository.findByUser(getUser(email));
    }

    @Transactional 
    public CartItem addToCart(String email, Long productId, int quantity) {
        User user = getUser(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existing = cartRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartRepository.save(item);
        }

        return cartRepository.save(CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build());
    }

    public CartItem updateQuantity(String email, Long itemId, int quantity) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (quantity <= 0) {
            cartRepository.delete(item);
            return null;
        }

        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    public void removeItem(String email, Long itemId) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        cartRepository.delete(item);
    }

    public void clearCart(String email) {
        cartRepository.deleteByUser(getUser(email));
    }

   public int getCartCount(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    return cartRepository.countByUser(user);
}


    public BigDecimal getCartTotal(String email) {
        return getCartItems(email).stream()
                .map(i -> i.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}