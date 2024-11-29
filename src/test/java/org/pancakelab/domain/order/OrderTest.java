package org.pancakelab.domain.order;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.pancake.PancakeBuilder;
import org.pancakelab.domain.pancake.Item;
import org.pancakelab.domain.shared.Address;
import org.pancakelab.domain.shared.Ingredient;
import org.pancakelab.domain.shared.IngredientName;

/**
 * @author Shantanu Singh.
 */
class OrderTest {
    private Order order;

    @BeforeEach
    void setUp() {
        Address address = new Address(1, 101);
        order = new Order(address);
    }

    @Test
    void testGivenOrderInitialized_ThenOrderStatusIsNew() {
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    @Test
    void testGivenOrderInitialized_WhenChocolatePancakeIsAdded_ThenOrderShouldHaveChocolatePancake() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        order.addItem(pancake);

        assertEquals(1, order.getItems().size());
    }

    @Test
    void testGivenOrderStatusIsCanceled_WhenChocolatePancakeIsAdded_ThenShouldThrowException() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();
        order.addItem(pancake);
        order.cancel();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            order.addItem(pancake);
        });

        assertEquals("Cannot add pancakes to an order that is not in NEW status.", exception.getMessage());
    }

    @Test
    void givenOrderWithPancakes_whenRemovingSpecificPancakes_thenPancakesAreRemoved() {
        var pancake1 = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        var pancake2 = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.WHIPPED_CREAM))
            .build();

        order.addItem(pancake1);
        order.addItem(pancake2);

        order.removeItem("Delicious pancake with dark chocolate!", 1);

        assertEquals(1, order.getItems().size());
    }

    @Test
    void givenOrderWithPancakes_whenRemovingSpecificPancakes_andOrderStatusChangedToDelivery_thenOrderCannotBeRemoved() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        order.addItem(pancake);
        order.complete();
        order.prepare();
        order.deliver();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            order.removeItem(pancake.getDescription(), 1);
        });

        assertEquals("Cannot remove pancakes from a completed order.", exception.getMessage());
    }

    @Test
    void givenNewOrder_whenStatusIsChangedToPreparing_thenStatusIsUpdated() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        order.addItem(pancake);
        order.complete();
        order.prepare();

        assertEquals(OrderStatus.PREPARED, order.getStatus());
    }

    @Test
    void givenNewOrderWithoutPancake_whenStatusIsChangedToComplete_thenThrowException() {
        Exception exception = assertThrows(IllegalStateException.class, order::complete);
        assertEquals("Cannot complete an order with no pancakes.", exception.getMessage());
    }

    @Test
    void givenNewOrder_whenStatusIsChangedToCompleted_thenStatusIsUpdated() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        order.addItem(pancake);
        order.complete();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void givenCompletedOrder_whenStatusIsChangedToDelivered_thenStatusIsUpdated() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        order.addItem(pancake);
        order.complete();
        order.prepare();
        order.deliver();

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void givenDeliveredOrder_whenStatusIsChanged_thenThrowsException() {
        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        order.addItem(pancake);
        order.complete();
        order.prepare();
        order.deliver();

        Exception exception = assertThrows(IllegalStateException.class, order::complete);
        assertEquals("Order must be in NEW state to complete.", exception.getMessage());
    }

    @Test
    void givenOrderWithMultiplePancakes_whenListingPancakes_thenDescriptionsAreReturned() {
        var pancake1 = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        var pancake2 = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.WHIPPED_CREAM))
            .build();

        order.addItem(pancake1);
        order.addItem(pancake2);

        // Act
        List<String> descriptions = order.getPancakeDescriptions();

        assertEquals(2, descriptions.size());
    }

    @Test
    void givenValidAddress_whenCreatingOrder_thenOrderIsCreatedWithAddress() {
        Address address = new Address(1, 101);
        Order order = new Order(address);

        assertEquals(address, order.getAddress());
    }

    @Test
    void givenOrder_whenAddingPancakesConcurrently_thenThreadSafe() throws InterruptedException {
        Order order = new Order(new Address(1, 101));
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable task = () -> {
            Item pancake = new PancakeBuilder()
                .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
                .build();
            order.addItem(pancake);
        };

        for (int i = 0; i < 10; i++) {
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(10, order.getItems().size());
    }

    @Test
    void givenOrder_whenStateTransitionsConcurrently_thenThreadSafe() throws InterruptedException {
        Order order = new Order(new Address(1, 101));
        order.addItem(new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.MILK_CHOCOLATE))
            .build());

        order.complete();

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable prepareTask = order::prepare;
        Runnable deliverTask = order::deliver;

        executor.submit(prepareTask);
        executor.submit(deliverTask);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void givenMultipleThreads_whenAddingAndRemovingPancakes_thenCorrectFinalCount() throws InterruptedException {
        Order order = new Order(new Address(1, 101));
        ExecutorService executor = Executors.newFixedThreadPool(10);

        int addCount = 50;
        int removeCount = 30;

        Runnable addTask = () -> {
            Item pancake = new PancakeBuilder().addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE)).build();
            order.addItem(pancake);
        };

        Runnable removeTask = () -> order.removeItem("Delicious pancake with dark chocolate!", 1);

        for (int i = 0; i < addCount; i++) {
            executor.submit(addTask);
        }

        for (int i = 0; i < removeCount; i++) {
            executor.submit(removeTask);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        int expectedCount = Math.max(0, addCount - removeCount);
        assertEquals(expectedCount, order.getItems().size());
    }

    @Test
    void givenMultipleReaders_whenReadingOrderStatus_thenNoContention() throws InterruptedException {
        Order order = new Order(new Address(1, 101));
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable readStatusTask = () -> assertNotNull(order.getStatus());

        for (int i = 0; i < 50; i++) {
            executor.submit(readStatusTask);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }





}