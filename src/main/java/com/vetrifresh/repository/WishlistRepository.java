package com.vetrifresh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vetrifresh.model.Product;
import com.vetrifresh.model.User;
import com.vetrifresh.model.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    int countByUser(User user);
} 