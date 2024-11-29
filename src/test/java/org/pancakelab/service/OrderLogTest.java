package org.pancakelab.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.log.OrderEvent;

/**
 * @author Shantanu Singh.
 */
class OrderLogTest {

    @Test
    void givenOrderEvent_whenLoggingEvent_thenEventIsStored() {
        OrderLog log = new OrderLog();
        UUID orderId = UUID.randomUUID();
        OrderEvent event = new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.ADD_PANCAKE, "Added pancake.");

        log.log(event);

        List<OrderEvent> events = log.getAllEvents();
        assertEquals(1, events.size());
        assertEquals(event, events.get(0));
    }

    @Test
    void givenMultipleEvents_whenQueryingByType_thenReturnsCorrectEvents() {
        OrderLog log = new OrderLog();
        UUID orderId = UUID.randomUUID();
        log.log(new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.ADD_PANCAKE, "Added pancake."));
        log.log(new OrderEvent(orderId, LocalDateTime.now(), OrderEvent.EventType.CANCEL_ORDER, "Order canceled."));

        List<OrderEvent> cancelEvents = log.getEventsByType(OrderEvent.EventType.CANCEL_ORDER);

        assertEquals(1, cancelEvents.size());
        assertEquals(OrderEvent.EventType.CANCEL_ORDER, cancelEvents.get(0).type());
    }

    @Test
    void givenMultipleEvents_whenQueryingByOrderId_thenReturnsCorrectEvents() {
        OrderLog log = new OrderLog();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        OrderEvent event1 = new OrderEvent(orderId1, LocalDateTime.now(), OrderEvent.EventType.ADD_PANCAKE, "Added dark chocolate pancake.");
        OrderEvent event2 = new OrderEvent(orderId2, LocalDateTime.now(), OrderEvent.EventType.CANCEL_ORDER, "Order canceled.");

        log.log(event1);
        log.log(event2);

        List<OrderEvent> eventsForOrder1 = log.getEventsForOrder(orderId1);

        assertEquals(1, eventsForOrder1.size());
        assertEquals(event1, eventsForOrder1.get(0));
    }




}