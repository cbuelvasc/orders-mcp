package dev.cbuelvasc.orders.domain.port.out;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

/** Outbound port: implemented by driven adapters (in-memory today, JPA tomorrow). */
public interface OrderRepositoryPort {

    Optional<Order> findById(OrderId id);

    List<Order> findRecentByCustomer(CustomerId customerId, int limit);
}
