package com.vetrifresh.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.vetrifresh.model.Product;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.service.CartService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor  
public class ProductDetailController {
     private final ProductRepository productRepository;
    private final CartService cartService;

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Related products — same category
        List<Product> related = productRepository
                .findTop4ByCategoryAndIdNotAndIsActiveTrue(
                        product.getCategory(), id);

        model.addAttribute("product", product);
        model.addAttribute("related", related);
        model.addAttribute("cartCount",
                userDetails != null ? cartService.getCartCount(userDetails.getUsername()) : 0);

        return "product-detail";
}
}