package org.pancakelab.domain.order;

/**
 * @author Shantanu Singh .
 */
public enum OrderStatus {
    NEW,            // Order has been created but not yet processed
    COMPLETED,      // Order is ready to prepare
    PREPARED,      // Order is ready for delivery
    DELIVERED,      // Order has been delivered
    CANCELLED       // Order has been cancelled
}
