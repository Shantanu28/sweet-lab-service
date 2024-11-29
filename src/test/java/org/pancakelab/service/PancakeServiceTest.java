package org.pancakelab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.log.OrderEvent;
import org.pancakelab.domain.order.Order;
import org.pancakelab.domain.order.OrderStatus;
import org.pancakelab.domain.shared.Address;
import org.pancakelab.domain.shared.Ingredient;
import org.pancakelab.domain.shared.IngredientName;
import org.pancakelab.repository.InMemoryOrderRepository;
import org.pancakelab.repository.OrderRepository;

public class PancakeServiceTest {
    private PancakeService  pancakeService;
    private Order           order;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

    @BeforeEach
    void setup() {
        OrderRepository orderRepository = new InMemoryOrderRepository();
        pancakeService = new PancakeService(orderRepository, new OrderLog());
        order = pancakeService.createOrder(new Address(1, 101));
    }

    @Test
    public void givenValidAddress_whenCreateOrder_thenOrderIsCreated() {
        Address address = new Address(10, 20);
        Order newOrder = pancakeService.createOrder(address);

        assertNotNull(newOrder);
        assertEquals(10, newOrder
            .getAddress()
            .building());
        assertEquals(20, newOrder
            .getAddress()
            .room());
    }

    @Test
    public void givenValidOrder_whenAddPancake_thenPancakesAreAdded() {
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        pancakeService.addCustomPancake(order.getId(), ingredients, 3);
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE)), 3);
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE),
            new Ingredient(IngredientName.HAZELNUTS)), 3);

        // verify
        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
            DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
            DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);
    }

    @Test
    void givenCanceledOrder_whenAddPancake_thenThrowsException() {
        pancakeService.cancelOrder(order.getId());
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));

        assertThrows(IllegalArgumentException.class, () ->
            pancakeService.addCustomPancake(order.getId(), ingredients, 1));
    }

    @Test
    public void givenPancakes_whenRemovePancakes_thenCorrectCountIsRemoved() {
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        pancakeService.addCustomPancake(order.getId(), ingredients, 3);
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE)), 3);
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE),
            new Ingredient(IngredientName.HAZELNUTS)), 3);

        // exercise
        pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 2);
        pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, order.getId(), 1);

        // verify
        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
            MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);
    }

    @Test
    public void givenNewOrder_whenCompleteOrder_thenStatusIsCompleted() {
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        pancakeService.addCustomPancake(order.getId(), ingredients, 3);
        // exercise
        pancakeService.completeOrder(order.getId());

        // verify
        Set<UUID> completedOrdersOrders = pancakeService.listCompletedOrders();
        assertTrue(completedOrdersOrders.contains(order.getId()));
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    public void givenCompletedOrder_whenPrepareOrder_thenStatusIsPrepared() {
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        pancakeService.addCustomPancake(order.getId(), ingredients, 3);
        pancakeService.completeOrder(order.getId());

        pancakeService.prepareOrder(order.getId());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertTrue(preparedOrders.contains(order.getId()));
        assertEquals(OrderStatus.PREPARED, order.getStatus());
    }

    @Test
    public void givenPreparedOrder_whenDeliverOrder_thenOrderIsDeliveredAndRemoved() {
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        pancakeService.addCustomPancake(order.getId(), ingredients, 3);
        pancakeService.completeOrder(order.getId());

        pancakeService.prepareOrder(order.getId());

        // setup
        List<String> pancakesToDeliver = pancakeService.viewOrder(order.getId());

        // exercise
        Object[] deliveredOrder = pancakeService.deliverOrder(order.getId());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.getId()));

        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(List.of(), ordersPancakes);
        assertEquals(order.getId(), ((Order) deliveredOrder[0]).getId());
        assertEquals(pancakesToDeliver, deliveredOrder[1]);
    }

    @Test
    public void givenOrderExists_WhenCancellingOrder_ThenOrderRemoved() {
        order = pancakeService.createOrder(new Address(10, 20));
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.DARK_CHOCOLATE)), 3);
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE)), 3);
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE),
            new Ingredient(IngredientName.HAZELNUTS)), 3);

        // exercise
        pancakeService.cancelOrder(order.getId());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.getId()));

        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(), ordersPancakes);
    }

    @Test
    public void givenValidOrder_WhenAddingDarkChocolatePancake_ThenPancakeIsAdded() {
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.DARK_CHOCOLATE)), 2);

        List<String> pancakes = pancakeService.viewOrder(order.getId());
        assertEquals(2, pancakes.size());
        assertEquals("Delicious pancake with dark chocolate!", pancakes.get(0));
    }

    @Test
    public void givenValidOrder_WhenAddingMilkChocolatePancake_ThenPancakeIsAdded() {
        pancakeService.addCustomPancake(order.getId(), List.of(new Ingredient(IngredientName.MILK_CHOCOLATE)), 3);

        List<String> pancakes = pancakeService.viewOrder(order.getId());
        assertEquals(3, pancakes.size());
        assertEquals("Delicious pancake with milk chocolate!", pancakes.get(0));

        assertEquals(3, order
            .getItems()
            .size());
    }

    @Test
    public void givenValidOrder_WhenAddingCustomPancake_ThenSuccess() {
        pancakeService.addCustomPancake(order.getId(), List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE),
            new Ingredient(IngredientName.HAZELNUTS)
        ), 2);

        List<String> pancakes = pancakeService.viewOrder(order.getId());
        assertEquals(2, pancakes.size());
        assertEquals("Delicious pancake with dark chocolate, hazelnuts!", pancakes.get(0));
    }

    @Test
    public void givenCancelledOrder_WhenAddingCustomPancake_ThenThrowException() {
        UUID orderId = order.getId();
        order.cancel();

        assertThrows(IllegalStateException.class, () -> pancakeService.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 1));
    }

    @Test
    public void givenOrderWithPancakes_whenViewingOrder_thenDescriptionsAreReturned() {
        UUID orderId = order.getId();

        pancakeService.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE),
            new Ingredient(IngredientName.WHIPPED_CREAM)
        ), 2);

        List<String> descriptions = pancakeService.viewOrder(orderId);

        assertEquals(2, descriptions.size());
        assertTrue(descriptions.contains("Delicious pancake with dark chocolate, whipped cream!"));
    }

    @Test
    public void givenOrderWithPancakes_whenRemovingPancakes_thenPancakesAreRemoved() {
        UUID orderId = order.getId();

        pancakeService.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);

        pancakeService.removePancakes("Delicious pancake with dark chocolate!", orderId, 1);

        assertEquals(1, order
            .getItems()
            .size());
    }

    @Test
    public void givenNewOrder_whenCompletedStatus_thenStatusCanBeUpdatedToCompleted() {
        UUID orderId = order.getId();
        pancakeService.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);
        pancakeService.completeOrder(orderId);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    public void givenCompletedOrder_whenPrepared_thenStatusIsUpdatedToPreparing() {
        UUID orderId = order.getId();
        pancakeService.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);

        pancakeService.completeOrder(orderId);
        pancakeService.prepareOrder(orderId);

        // Assert
        assertEquals(OrderStatus.PREPARED, order.getStatus());
    }

    @Test
    public void givenCompletedOrder_whenDeliveringOrder_thenStatusIsUpdatedToDelivered() {
        UUID orderId = order.getId();
        pancakeService.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);
        pancakeService.completeOrder(orderId);
        pancakeService.prepareOrder(orderId);

        List<String> pancakesToDeliver = pancakeService.viewOrder(order.getId());

        Object[] deliveredOrder = pancakeService.deliverOrder(orderId);

        // Assert
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(order.getId(), ((Order) deliveredOrder[0]).getId());
        assertEquals(pancakesToDeliver, deliveredOrder[1]);
    }

    @Test
    public void givenMultipleOrders_whenListingCompletedOrders_thenOnlyCompletedOrderIdsAreReturned() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        Order order1 = service.createOrder(new Address(1, 101));
        Order order2 = service.createOrder(new Address(1, 102));
        UUID order1Id = order1.getId();
        UUID order2Id = order2.getId();

        service.addCustomPancake(order1Id, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);
        service.addCustomPancake(order2Id, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);

        service.completeOrder(order1Id);

        Set<UUID> completedOrders = service.listCompletedOrders();

        assertEquals(1, completedOrders.size());
        assertTrue(completedOrders.contains(order1Id));
        assertFalse(completedOrders.contains(order2Id));
    }

    @Test
    public void givenMultipleOrders_whenListingPreparedOrders_thenOnlyPreparedOrderIdsAreReturned() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        Order order1 = service.createOrder(new Address(1, 101));
        Order order2 = service.createOrder(new Address(1, 102));
        UUID order1Id = order1.getId();
        UUID order2Id = order2.getId();

        service.addCustomPancake(order1Id, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);
        service.addCustomPancake(order2Id, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);

        service.completeOrder(order2Id);
        service.prepareOrder(order2Id);

        Set<UUID> preparedOrders = service.listPreparedOrders();

        assertEquals(1, preparedOrders.size());
        assertFalse(preparedOrders.contains(order1Id));
        assertTrue(preparedOrders.contains(order2Id));
    }

    @Test
    public void givenNewOrder_whenCancellingOrder_thenOrderIsRemovedFromTracking() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        Order order = service.createOrder(new Address(1, 101));
        UUID orderId = order.getId();
        service.addCustomPancake(orderId, List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE)
        ), 2);

        service.cancelOrder(orderId);

        // Assert
        assertEquals(List.of(), service.viewOrder(orderId));
    }

    @Test
    public void givenOrder_whenAddingCustomPancake_thenEventIsLogged() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        UUID orderId = service
            .createOrder(new Address(1, 101))
            .getId();
        service.addCustomPancake(orderId, List.of(new Ingredient(IngredientName.DARK_CHOCOLATE)), 1);

        List<OrderEvent> events = service
            .getOrderLog()
            .getEventsForOrder(orderId);
        assertEquals(1, events.size());
        assertEquals(OrderEvent.EventType.ADD_PANCAKE, events
            .get(0)
            .type());
        assertEquals("Added pancake with description Delicious pancake with dark chocolate!", events
            .get(0)
            .details());
    }

    @Test
    public void givenOrder_whenCancelingOrder_thenEventIsLogged() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        UUID orderId = service
            .createOrder(new Address(1, 101))
            .getId();

        service.cancelOrder(orderId);

        List<OrderEvent> events = service
            .getOrderLog()
            .getEventsForOrder(orderId);
        assertEquals(1, events.size());
        assertEquals(OrderEvent.EventType.CANCEL_ORDER, events
            .get(0)
            .type());
        assertEquals("Order canceled with 0 pancakes with orderId " + orderId + ".", events
            .get(0)
            .details());
    }

    @Test
    public void givenOrderWithPancakes_whenRemovingPancakes_thenEventIsLogged() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        UUID orderId = service
            .createOrder(new Address(1, 101))
            .getId();
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        service.addCustomPancake(orderId, ingredients, 3);

        service.removePancakes("Delicious pancake with dark chocolate!", orderId, 2);

        List<OrderEvent> events = service
            .getOrderLog()
            .getEventsForOrder(orderId);
        assertEquals(4, events.size());
        OrderEvent lastEvent = events.get(events.size() - 1);
        assertEquals(OrderEvent.EventType.REMOVE_PANCAKE, lastEvent.type());
        assertEquals(
            "Removed 2 pancake(s) with description 'Delicious pancake with dark chocolate!'. Order now contains 1 pancake(s).",
            lastEvent.details()
        );
    }

    @Test
    public void givenPreparedOrder_whenDeliveringOrder_thenEventIsLogged() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        UUID orderId = service
            .createOrder(new Address(1, 101))
            .getId();
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));
        service.addCustomPancake(orderId, ingredients, 3);
        service.completeOrder(orderId);
        service.prepareOrder(orderId);

        service.deliverOrder(orderId);

        List<OrderEvent> events = service
            .getOrderLog()
            .getEventsForOrder(orderId);
        assertEquals(4, events.size());
        OrderEvent lastEvent = events.get(events.size() - 1);
        assertEquals(OrderEvent.EventType.DELIVER_ORDER, lastEvent.type());
        assertEquals(
            String.format("Order %s with 3 pancake(s) delivered to building 1, room 101.", orderId),
            lastEvent.details()
        );
    }

    @Test
    public void givenOrder_whenAddingAndDelivering_thenLogsCorrectlyAndRemovesOrder() {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        Address address = new Address(1, 101);
        UUID orderId = service
            .createOrder(address)
            .getId();
        List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.DARK_CHOCOLATE));

        service.addCustomPancake(orderId, ingredients, 3);
        service.completeOrder(orderId);
        service.prepareOrder(orderId);
        service.deliverOrder(orderId);

        assertEquals(List.of(), service.viewOrder(orderId));
    }

    @Test
    public void givenMultipleThreads_whenModifyingOrders_thenNoRaceConditions() throws InterruptedException {
        PancakeService service = new PancakeService(new InMemoryOrderRepository(), new OrderLog());
        Address address = new Address(1, 101);
        UUID orderId = service
            .createOrder(address)
            .getId();

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable addTask = () -> {
            List<Ingredient> ingredients = List.of(new Ingredient(IngredientName.MILK_CHOCOLATE));
            service.addCustomPancake(orderId, ingredients, 1);
        };

        Runnable prepareTask = () -> service.prepareOrder(orderId);
        Runnable completeTask = () -> service.completeOrder(orderId);

        executor.submit(addTask);
        executor.submit(completeTask);
        executor.submit(prepareTask);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, service
            .viewOrder(orderId)
            .size());
    }

}
