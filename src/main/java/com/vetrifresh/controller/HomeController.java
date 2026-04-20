package com.vetrifresh.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vetrifresh.dto.RegisterRequest;
import com.vetrifresh.model.Blog;
import com.vetrifresh.model.User;
import com.vetrifresh.repository.BlogRepository;
import com.vetrifresh.repository.CategoryRepository;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.repository.UserRepository;
import com.vetrifresh.service.AuthService;
import com.vetrifresh.service.CartService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AuthService authService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CartService cartService;


    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    @Transactional(readOnly = true)
    public String homePage(Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {

        model.addAttribute("products", productRepository.findByIsFeaturedTrue());
        model.addAttribute("categories", categoryRepository.findAll());

        // cart count for header badge
        if (userDetails != null) {
            model.addAttribute("cartCount", cartService.getCartCount(userDetails.getUsername()));
        } else {
            model.addAttribute("cartCount", 0);
        }

        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute RegisterRequest registerRequest,
                                 Model model) {
        try {
            authService.register(registerRequest);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("registerRequest", registerRequest);
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }


    @GetMapping("/contact")
public String contactPage() {
    return "contact";
}

@PostMapping("/contact/submit")
public String contactSubmit(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam String message,
                            Model model) {
    try {
        SimpleMailMessage mail = new SimpleMailMessage();

        // Email sent TO your business email
        mail.setTo("manikandarajnatraj@gmail.com");
        mail.setSubject("New Contact Message from " + name);
        mail.setText(
            "Name:    " + name    + "\n" +
            "Email:   " + email   + "\n" +
            "Phone:   " + phone   + "\n\n" +
            "Message:\n" + message
        );

        // Reply-to set to customer's email
        mail.setReplyTo(email);

        mailSender.send(mail);

        model.addAttribute("successMessage",
            "Thank you " + name + "! Your message has been sent successfully.");

    } catch (Exception e) {
        model.addAttribute("errorMessage",
            "Something went wrong. Please try again.");
    }

    return "contact";
}



   @GetMapping("/about")
public String AboutPage() {
    return "about";
}

@GetMapping("/blog")
@Transactional(readOnly = true)
public String blogPage(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String tag,
        Model model) {

    List<Blog> blogs = blogRepository.findByIsPublishedTrueOrderByCreatedAtDesc();

    // Static default tags (always shown)
    List<String> staticTags = Arrays.asList(
        "Healthy", "Low fat", "Vegetarian", "Kid foods",
        "Vitamins", "Bread", "Meat", "Snacks",
        "Tiffin", "Launch", "Dinner", "Breakfast", "Fruit"
    );

    // Dynamic tags from DB
    List<String> dbTags = blogs.stream()
        .filter(b -> b.getTags() != null && !b.getTags().isBlank())
        .flatMap(b -> Arrays.stream(b.getTags().split(",")))
        .map(String::trim)
        .filter(t -> !t.isEmpty())
        .collect(Collectors.toList());

    // Merge both — no duplicates, sorted
    List<String> allTags = Stream.concat(staticTags.stream(), dbTags.stream())
        .map(String::trim)
        .distinct()
        .sorted()
        .collect(Collectors.toList());

    // Apply filters
    if (category != null && !category.isBlank()) {
        blogs = blogs.stream()
            .filter(b -> category.equalsIgnoreCase(b.getCategory()))
            .collect(Collectors.toList());
    }
    if (tag != null && !tag.isBlank()) {
        blogs = blogs.stream()
            .filter(b -> b.getTags() != null &&
                Arrays.stream(b.getTags().split(","))
                      .map(String::trim)
                      .anyMatch(t -> t.equalsIgnoreCase(tag)))
            .collect(Collectors.toList());
    }

    model.addAttribute("blogs", blogs);
    model.addAttribute("allTags", allTags);
    model.addAttribute("activeTag", tag);
    return "blog";
}


@GetMapping("/blog/{id}")
public String blogDetail(@PathVariable Long id, Model model) {
    Blog blog = blogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blog not found"));

    List<Blog> recent = blogRepository
            .findByIsPublishedTrueOrderByCreatedAtDesc()
            .stream().limit(3).toList();

    model.addAttribute("blog", blog);
    model.addAttribute("recentPosts", recent);
    return "blog-detail";
}


@GetMapping("/faq")
public String faq() { return "faq"; }

@GetMapping("/terms")
public String terms() { return "terms"; }

@GetMapping("/privacy")
public String privacy() { return "privacy"; }


// @GetMapping("/profile")
// public String profile(Model model, Principal principal) {
//     if (principal == null) return "redirect:/login";
//     User user = userRepository.findByEmail(principal.getName())
//                               .orElseThrow();
//     model.addAttribute("user", user);
//     return "profile";
// }


@ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}
}