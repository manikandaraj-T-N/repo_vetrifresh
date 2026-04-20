package com.vetrifresh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vetrifresh.model.Category;

import com.vetrifresh.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsFeaturedTrue();
    List<Product> findByIsActiveTrue();
    List<Product> findByCategory_Id(Long categoryId);
    List<Product> findByStockQuantityLessThan(int threshold);
    List<Product> findTop5ByOrderByCreatedAtDesc();


    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);

    List<Product> findByIsFeaturedTrueAndIsActiveTrue();

    List<Product> findByIsOrganicTrueAndIsActiveTrue();

    List<Product> findByStockQuantityLessThanAndIsActiveTrue(int qty);

    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    List<Product> findTop4ByCategoryAndIdNotAndIsActiveTrue(Category category, Long id);


    
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsActiveTrue(
            String name, String description);

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.discountPercentage > 0 AND p.isActive = true")
    List<Product> findProductsOnSale();

}
