package org.pancakelab.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.pancakelab.domain.order.Order;

/**
 * @author Shantanu Singh.
 */
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<UUID, Order> orderMap = new ConcurrentHashMap<>();

    @Override public Order findById(UUID orderId) {
        return orderMap.get(orderId);
    }

    @Override public void save(Order order) {
        orderMap.put(order.getId(), order);
    }

    @Override public void delete(UUID orderId) {
        orderMap.remove(orderId);
    }

    @Override public List<Order> findAll() {
        return new ArrayList<>(orderMap.values());
    }
}
