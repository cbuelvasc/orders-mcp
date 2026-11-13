package dev.cbuelvasc.orders.infrastructure.adapter.out.memory;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Test double of the outbound port. It must pass the same contract as the real adapter. */
public class InMemoryOrderRepository implements OrderRepositoryPort {

    private final Map<OrderId, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findRecentByCustomer(CustomerId customerId, int limit) {
        return orders.values().stream()
                .filter(order -> order.customerId().equals(customerId))
                .sorted(Comparator.comparing(Order::updatedAt).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public Order save(Order order) {
        orders.put(order.id(), order);
        return order;
    }
}
