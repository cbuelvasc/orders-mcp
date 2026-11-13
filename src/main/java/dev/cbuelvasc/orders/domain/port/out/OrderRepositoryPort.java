package dev.cbuelvasc.orders.domain.port.out;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

/** Outbound port: implemented by driven adapters (PostgreSQL in production, in-memory in tests). */
public interface OrderRepositoryPort {

    Optional<Order> findById(OrderId id);

    List<Order> findRecentByCustomer(CustomerId customerId, int limit);

    Order save(Order order);
}
