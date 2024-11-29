package org.pancakelab.domain.log;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Shantanu Singh.
 */
public record OrderEvent(UUID orderId,
                         LocalDateTime timestamp,
                         EventType type,
                         String details
) {
    public enum EventType {
        ADD_PANCAKE,
        REMOVE_PANCAKE,
        CANCEL_ORDER,
        DELIVER_ORDER
    }
}