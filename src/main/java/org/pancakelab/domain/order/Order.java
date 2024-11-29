package org.pancakelab.domain.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.pancakelab.domain.pancake.Item;
import org.pancakelab.domain.shared.Address;

public class Order {
    private final UUID        id;
    private final List<Item>  items;
    private       OrderStatus status;
    private final Address       address;
    private final Lock itemLock = new ReentrantLock();
    private final Lock statusLock   = new ReentrantLock();

    public Order(final Address address) {
        this.id = UUID.randomUUID();
        this.address = address;
        this.status = OrderStatus.NEW;
        this.items = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override public int hashCode() {
        return Objects.hashCode(id);
    }

    public OrderStatus getStatus() {
        statusLock.lock();
        try {
            return status;
        }
        finally {
            statusLock.unlock();
        }
    }

    public void addItem(Item pancake) {
        itemLock.lock();
        try {
            if (status != OrderStatus.NEW) {
                throw new IllegalStateException("Cannot add pancakes to an order that is not in NEW status.");
            }
            items.add(pancake);
        }
        finally {
            itemLock.unlock();
        }
    }

    public List<Item> getItems() {
        itemLock.lock();
        try {
            return Collections.unmodifiableList(items);
        }
        finally {
            itemLock.unlock();
        }
    }

    public void removeItem(String description, int count) {
        itemLock.lock();
        try {
            if (status != OrderStatus.NEW) {
                throw new IllegalStateException("Cannot remove pancakes from a completed order.");
            }

            int removed = 0;
            Iterator<Item> iterator = items.iterator();
            while (iterator.hasNext() && removed < count) {
                Item item = iterator.next();
                if (item
                    .getDescription()
                    .equals(description)) {
                    iterator.remove();
                    removed++;
                }
            }
        }
        finally {
            itemLock.unlock();
        }
    }

    public void complete() {
        statusLock.lock();
        try {
            if (status != OrderStatus.NEW) {
                throw new IllegalStateException("Order must be in NEW state to complete.");
            }
            if (items.isEmpty()) {
                throw new IllegalStateException("Cannot complete an order with no pancakes.");
            }

            status = OrderStatus.COMPLETED;
        }
        finally {
            statusLock.unlock();
        }
    }

    public void prepare() {
        statusLock.lock();
        try {
            if (status != OrderStatus.COMPLETED) {
                throw new IllegalStateException("Order must be in COMPLETED state to prepare.");
            }
            status = OrderStatus.PREPARED;
        }
        finally {
            statusLock.unlock();
        }
    }

    public void deliver() {
        statusLock.lock();
        try {
            if (status != OrderStatus.PREPARED) {
                throw new IllegalStateException("Order must be in PREPARED state to deliver.");
            }

            status = OrderStatus.DELIVERED;
        }
        finally {
            statusLock.unlock();
        }
    }

    public void cancel() {
        statusLock.lock();
        try {
            if (status != OrderStatus.NEW) {
                throw new IllegalStateException("Cannot cancel the order.");
            }
            status = OrderStatus.CANCELLED;
        }
        finally {
            statusLock.unlock();
        }
    }

    public List<String> getPancakeDescriptions() {
        itemLock.lock();
        try {
            return items
                .stream()
                .map(Item::getDescription)
                .toList();
        }
        finally {
            itemLock.unlock();
        }
    }

    public Address getAddress() {
        return address;
    }
}
