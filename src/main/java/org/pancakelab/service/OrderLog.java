package org.pancakelab.service;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.pancakelab.domain.log.OrderEvent;

public class OrderLog {
    private final Queue<OrderEvent> events = new ConcurrentLinkedQueue<>();

    public void log(OrderEvent event) {
        events.add(event);
    }

    public List<OrderEvent> getEventsForOrder(UUID orderId) {
        return events
            .stream()
            .filter(event -> event
                .orderId()
                .equals(orderId))
            .toList();
    }

    public List<OrderEvent> getEventsByType(OrderEvent.EventType type) {
        return events
            .stream()
            .filter(event -> event.type() == type)
            .toList();
    }

    public List<OrderEvent> getAllEvents() {
        return List.copyOf(events);
    }
}
