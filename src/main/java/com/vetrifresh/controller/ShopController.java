package com.vetrifresh.controller;

import java.util.stream.Collectors;

import com.vetrifresh.model.Product;
import com.vetrifresh.model.User;
import com.vetrifresh.repository.CategoryRepository;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.repository.UserRepository;


import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @GetMapping("/shop")
public String shop(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) Integer rating,
        @RequestParam(required = false) String tag,
        @RequestParam(defaultValue = "latest") String sort,
        Model model) {

    List<Product> products = productRepository.findAll();

    // Filter by category
    if (category != null && !category.equals("all")) {
        products = products.stream()
            .filter(p -> p.getCategory() != null
                      && p.getCategory().getSlug().equals(category))
            .collect(Collectors.toList());
    }

    // Filter by price
    if (maxPrice != null) {
        BigDecimal max = BigDecimal.valueOf(maxPrice);
        products = products.stream()
            .filter(p -> p.getPrice() != null && p.getPrice().compareTo(max) <= 0)
            .collect(Collectors.toList());
    }

    // Filter by rating
    if (rating != null) {
        BigDecimal minRating = BigDecimal.valueOf(rating);
        products = products.stream()
            .filter(p -> p.getRating() != null
                      && p.getRating().compareTo(minRating) >= 0)
            .collect(Collectors.toList());
    }

    // Filter by tag (if Product has a tags field)
    if (tag != null && !tag.isBlank()) {
        products = products.stream()
            .filter(p -> p.getTags() != null &&
                Arrays.stream(p.getTags().split(","))
                      .map(String::trim)
                      .anyMatch(t -> t.equalsIgnoreCase(tag)))
            .collect(Collectors.toList());
    }

    // Sort
    switch (sort) {
        case "price-low"  -> products.sort(
            Comparator.comparing(p -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice())
        );
        case "price-high" -> products.sort(
            Comparator.comparing((Product p) ->
                p.getPrice() == null ? BigDecimal.ZERO : p.getPrice()
            ).reversed()
        );
        case "rating"     -> products.sort(
            Comparator.comparing((Product p) ->
                p.getRating() == null ? BigDecimal.ZERO : p.getRating()
            ).reversed()
        );
    }

    // Static default tags
    List<String> staticTags = Arrays.asList(
        "Healthy", "Low fat", "Vegetarian", "Kid foods",
        "Vitamins", "Bread", "Meat", "Snacks",
        "Tiffin", "Launch", "Dinner", "Breakfast", "Fruit"
    );

    // Dynamic tags from products (if Product has tags field)
    List<String> dbTags = productRepository.findAll().stream()
        .filter(p -> p.getTags() != null && !p.getTags().isBlank())
        .flatMap(p -> Arrays.stream(p.getTags().split(",")))
        .map(String::trim)
        .filter(t -> !t.isEmpty())
        .collect(Collectors.toList());

    // Merge static + dynamic, no duplicates, sorted
    List<String> allTags = Stream.concat(staticTags.stream(), dbTags.stream())
        .map(String::trim)
        .distinct()
        .sorted()
        .collect(Collectors.toList());

    model.addAttribute("products", products);
    model.addAttribute("categories", categoryRepository.findAll());
    model.addAttribute("selectedCategory", category);
    model.addAttribute("selectedRating", rating);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("sort", sort);
    model.addAttribute("totalProducts", products.size());
    model.addAttribute("allTags", allTags);
    model.addAttribute("activeTag", tag);

    return "shop";
}


@ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}


}