package org.pancakelab.repository;

import java.util.List;
import java.util.UUID;
import org.pancakelab.domain.order.Order;

/**
 * @author Shantanu Singh.
 */
public interface OrderRepository {

    Order findById(UUID orderId);

    void save(Order order);

    void delete(UUID orderId);

    List<Order> findAll();
}
