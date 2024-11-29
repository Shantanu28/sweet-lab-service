package org.pancakelab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.pancakelab.domain.log.OrderEvent;
import org.pancakelab.domain.order.Order;
import org.pancakelab.domain.order.OrderStatus;
import org.pancakelab.domain.pancake.Item;
import org.pancakelab.domain.pancake.PancakeBuilder;
import org.pancakelab.domain.shared.Address;
import org.pancakelab.domain.shared.Ingredient;
import org.pancakelab.repository.OrderRepository;

public class PancakeService {
    private final OrderRepository orderRepository;
    private final OrderLog        orderLog;

    public PancakeService(OrderRepository orderRepository, OrderLog orderLog) {
        this.orderRepository = orderRepository;
        this.orderLog = orderLog;
    }

    public Order createOrder(final Address address) {
        Order order = new Order(address);
        this.orderRepository.save(order);
        return order;
    }

    public void addCustomPancake(UUID orderId, List<Ingredient> ingredients, int count) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        for (int i = 0; i < count; ++i) {
            PancakeBuilder pancakeBuilder = new PancakeBuilder();
            for (Ingredient ingredient : ingredients) {
                pancakeBuilder.addIngredient(ingredient);
            }
            Item pancake = pancakeBuilder.build();
            order.addItem(pancake);
            orderLog.log(new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.ADD_PANCAKE,
                "Added pancake with description %s".formatted(pancake.getDescription())));
        }
    }

    public List<String> viewOrder(UUID orderId) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            return List.of();
        }
        return order.getPancakeDescriptions();
    }

    public void removePancakes(String description, UUID orderId, int count) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        int initialSize = order
            .getItems()
            .size();
        order.removeItem(description, count);
        int removedCount = initialSize - order
            .getItems()
            .size();

        orderLog.log(new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.REMOVE_PANCAKE,
            String.format("Removed %d pancake(s) with description '%s'. Order now contains %d pancake(s).", removedCount, description, order
                .getItems()
                .size())));
    }

    public void cancelOrder(UUID orderId) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        order.cancel();
        this.orderRepository.delete(orderId);
        orderLog.log(new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.CANCEL_ORDER,
            "Order canceled with %d pancakes with orderId %s.".formatted(order
                .getItems()
                .size(), orderId)));
    }

    public void completeOrder(UUID orderId) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        order.complete();
    }

    public Set<UUID> listCompletedOrders() {
        return this.orderRepository
            .findAll()
            .stream()
            .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
            .map(Order::getId)
            .collect(Collectors.toSet());
    }

    public void prepareOrder(UUID orderId) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        order.prepare();
    }

    public Set<UUID> listPreparedOrders() {
        return this.orderRepository
            .findAll()
            .stream()
            .filter(order -> order.getStatus() == OrderStatus.PREPARED)
            .map(Order::getId)
            .collect(Collectors.toSet());
    }

    public Object[] deliverOrder(UUID orderId) {
        Order order = this.orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        List<String> pancakesToDeliver = viewOrder(orderId);
        order.deliver();

        orderLog.log(new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.DELIVER_ORDER,
            String.format("Order %s with %d pancake(s) delivered to building %d, room %d.", orderId, order
                .getItems()
                .size(), order
                .getAddress()
                .building(), order
                .getAddress()
                .room())));

        this.orderRepository.delete(orderId);

        return new Object[] { order, pancakesToDeliver };
    }

    public OrderLog getOrderLog() {
        return orderLog;
    }
}
