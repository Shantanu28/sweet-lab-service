package org.pancakelab.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.order.Order;
import org.pancakelab.domain.pancake.PancakeBuilder;
import org.pancakelab.domain.shared.Address;
import org.pancakelab.domain.shared.Ingredient;
import org.pancakelab.domain.shared.IngredientName;

/**
 * @author Shantanu Singh.
 */
class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    void givenNewOrder_whenSaved_thenOrderCanBeRetrieved() {
        Address address = new Address(1, 101);
        Order order = new Order(address);

        repository.save(order);

        Order retrievedOrder = repository.findById(order.getId());
        assertNotNull(retrievedOrder);
        assertEquals(order, retrievedOrder);
    }

    @Test
    void givenOrderId_whenOrderDoesNotExist_thenReturnNull() {
        Order retrievedOrder = repository.findById(UUID.randomUUID());

        assertNull(retrievedOrder);
    }

    @Test
    void givenMultipleOrders_whenSaved_thenAllOrdersCanBeRetrieved() {
        Order order1 = new Order(new Address(1, 101));
        Order order2 = new Order(new Address(2, 202));

        repository.save(order1);
        repository.save(order2);

        List<Order> allOrders = repository.findAll();
        assertEquals(2, allOrders.size());
        assertTrue(allOrders.contains(order1));
        assertTrue(allOrders.contains(order2));
    }

    @Test
    void givenExistingOrder_whenDeleted_thenOrderCannotBeRetrieved() {
        Order order = new Order(new Address(1, 101));
        repository.save(order);

        repository.delete(order.getId());

        assertNull(repository.findById(order.getId()));
    }

    @Test
    void givenExistingOrder_whenSavedAgain_thenOrderIsUpdated() {
        Order order = new Order(new Address(1, 101));
        repository.save(order);

        var pancake = new PancakeBuilder()
            .addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();
        order.addItem(pancake);
        repository.save(order);

        Order updatedOrder = repository.findById(order.getId());
        assertNotNull(updatedOrder);
        assertEquals(order, updatedOrder);
        assertEquals(1, updatedOrder.getItems().size());
    }

    @Test
    void givenNonExistentOrder_whenDeleted_thenNoErrorIsThrown() {
        assertDoesNotThrow(() -> repository.delete(UUID.randomUUID()));
    }

}