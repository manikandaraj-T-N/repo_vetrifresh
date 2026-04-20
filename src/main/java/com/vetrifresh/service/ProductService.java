package com.vetrifresh.service;

import com.vetrifresh.model.Category;
import com.vetrifresh.model.Product;
import com.vetrifresh.repository.CategoryRepository;
import com.vetrifresh.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }

    public List<Product> getOrganicProducts() {
        return productRepository.findByIsOrganicTrueAndIsActiveTrue();
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public List<Product> getProductsOnSale() {
        return productRepository.findProductsOnSale();
    }

    public Product createProduct(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setOriginalPrice(updatedProduct.getOriginalPrice());
        existing.setStockQuantity(updatedProduct.getStockQuantity());
        existing.setImageUrl(updatedProduct.getImageUrl());
        existing.setIsOrganic(updatedProduct.getIsOrganic());
        existing.setIsFeatured(updatedProduct.getIsFeatured());
        existing.setDiscountPercentage(updatedProduct.getDiscountPercentage());
        existing.setUnit(updatedProduct.getUnit());
        if (updatedProduct.getCategory() != null) {
            existing.setCategory(updatedProduct.getCategory());
        }
        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setIsActive(false); // soft delete
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanAndIsActiveTrue(threshold);
    }
}
