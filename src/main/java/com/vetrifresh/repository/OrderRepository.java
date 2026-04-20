package com.vetrifresh.repository;

import com.vetrifresh.model.Order;
import com.vetrifresh.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    
}
