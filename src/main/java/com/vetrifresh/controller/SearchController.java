package com.vetrifresh.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vetrifresh.model.Product;
import com.vetrifresh.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ProductRepository productRepository;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {

        List<Product> results = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            results = productRepository
                    .findByNameContainingIgnoreCaseAndIsActiveTrue(q.trim());
        }

        model.addAttribute("results", results);
        model.addAttribute("query", q);
        // model.addAttribute("cartCount", 0);
        return "search";
    }
}