package dev.cbuelvasc.orders.application;

import dev.cbuelvasc.orders.domain.exception.OrderNotFoundException;
import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.in.FindOrderUseCase;
import dev.cbuelvasc.orders.domain.port.in.ListRecentOrdersUseCase;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;

import java.util.List;
import java.util.Objects;

/**
 * Application service. No Spring annotations: it is wired in infrastructure/config,
 * so the core stays framework-agnostic (verified by HexagonalArchitectureTest).
 */
public class OrderQueryService implements FindOrderUseCase, ListRecentOrdersUseCase {

    private final OrderRepositoryPort repository;

    public OrderQueryService(OrderRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Order findById(OrderId id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public List<Order> listRecent(CustomerId customerId, int limit) {
        // Business rule lives in the core, so it protects EVERY adapter (REST, MCP, ...)
        int safeLimit = Math.clamp(limit, 1, MAX_LIMIT);
        return repository.findRecentByCustomer(customerId, safeLimit);
    }
}
