package com.vetrifresh.controller;

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

import com.vetrifresh.model.Blog;
import com.vetrifresh.model.Category;
import com.vetrifresh.model.Product;
import com.vetrifresh.model.User;
import com.vetrifresh.repository.BlogRepository;
import com.vetrifresh.repository.CategoryRepository;
import com.vetrifresh.repository.ProductRepository;
import com.vetrifresh.repository.UserRepository;
import com.vetrifresh.service.ProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private final ProductService productService;

    private final BlogRepository blogRepository;

    // ─── Dashboard ───────────────────────────────────────
   @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts",   productRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("featuredCount",   productRepository.findByIsFeaturedTrue().size());
        model.addAttribute("lowStockCount",   productRepository.findByStockQuantityLessThan(10).size());
        model.addAttribute("recentProducts",  productRepository.findTop5ByOrderByCreatedAtDesc());
        return "admin/dashboard";
    }

    // ─── Product List ─────────────────────────────────────
    @GetMapping("/products")
    public String productList(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "admin/products";
    }

    // ─── Add Product Form ─────────────────────────────────
    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product",    new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    // ─── Edit Product Form ────────────────────────────────
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product",    product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    // ─── Save Product (Add + Edit) ────────────────────────
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String isFeatured,
                              @RequestParam(required = false) String isOrganic,
                              @RequestParam(required = false) String isActive) {

        // Set booleans from checkbox (checkbox sends value only if checked)
        product.setIsFeatured(isFeatured != null);
        product.setIsOrganic(isOrganic   != null);
        product.setIsActive(isActive     != null);

        // Set category
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .ifPresent(product::setCategory);
        }

        productRepository.save(product);
        return "redirect:/admin/products";
    }

    // ─── Delete Product ───────────────────────────────────
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }

    // ─── Toggle Featured ──────────────────────────────────
    @PostMapping("/products/toggle-featured/{id}")
    public String toggleFeatured(@PathVariable Long id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setIsFeatured(!Boolean.TRUE.equals(p.getIsFeatured()));
            productRepository.save(p);
        });
        return "redirect:/admin/products";
    }

    // ─── Category List ────────────────────────────────────
    @GetMapping("/categories")
    public String categoryList(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories";
    }

    // ─── Add Category Form ────────────────────────────────
    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    // ─── Edit Category Form ───────────────────────────────
    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        return "admin/category-form";
    }

    // ─── Save Category ────────────────────────────────────
    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam(required = false) String isActive) {
        category.setIsActive(isActive != null);
        // Auto-generate slug from name
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(category.getName().toLowerCase().replace(" ", "-"));
        }
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    // ─── Delete Category ──────────────────────────────────
    @PostMapping("/categories/delete/{id}")
    @Transactional
public String deleteCategory(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {

    // 1️⃣ Delete all products under this category
    productRepository.deleteByCategoryId(id);

    // 2️⃣ Delete the category
    categoryRepository.deleteById(id);

    redirectAttributes.addFlashAttribute("success",
            " Category and all its products deleted successfully.");

    return "redirect:/admin/categories";
}

// ─── Blog List ────────────────────────────────────
@GetMapping("/blogs")
public String blogList(Model model) {
    model.addAttribute("blogs", blogRepository.findAll());
    return "admin/blogs";
}

// ─── New Blog Form ────────────────────────────────
@GetMapping("/blogs/new")
public String newBlogForm(Model model) {
    model.addAttribute("blog", new Blog());
    return "admin/blog-form";
}

// ─── Edit Blog Form ───────────────────────────────
@GetMapping("/blogs/edit/{id}")
public String editBlogForm(@PathVariable Long id, Model model) {
    Blog blog = blogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blog not found"));
    model.addAttribute("blog", blog);
    return "admin/blog-form";
}

// ─── Save Blog ────────────────────────────────────
@PostMapping("/blogs/save")
public String saveBlog(@ModelAttribute Blog blog,
                       @RequestParam(required = false) String isPublished) {
    blog.setIsPublished(isPublished != null);
    if (blog.getAuthor() == null || blog.getAuthor().isBlank()) {
        blog.setAuthor("Admin");
    }
    blogRepository.save(blog);
    return "redirect:/admin/blogs";
}

// ─── Delete Blog ──────────────────────────────────
@PostMapping("/blogs/delete/{id}")
public String deleteBlog(@PathVariable Long id) {
    blogRepository.deleteById(id);
    return "redirect:/admin/blogs";
}  

@ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}



}