package com.pcforge.pedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pcforge.pedidos.domain.Order;
import com.pcforge.pedidos.domain.OrderStatus;

public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByAssignedTechnicianIdOrderByCreatedAtAsc(String technicianId);

    List<Order> findAllByOrderByCreatedAtDesc();
}
