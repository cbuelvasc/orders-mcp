package dev.cbuelvasc.orders.application;

import dev.cbuelvasc.orders.domain.exception.OrderNotFoundException;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.in.CancelOrderUseCase;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;

import java.time.Clock;
import java.util.Objects;

/** Command side. The rule lives in the aggregate (Order.cancel); the service only orchestrates. */
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepositoryPort repository;
    private final Clock clock;

    public CancelOrderService(OrderRepositoryPort repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Order cancel(OrderId id) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return repository.save(order.cancel(clock.instant()));
    }
}
