package dev.cbuelvasc.orders.infrastructure.adapter.out.memory;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Driven adapter with demo data, so the project runs with zero infrastructure.
 * Swap it for a JPA/JDBC adapter without touching the core.
 */
@Component
public class InMemoryOrderRepository implements OrderRepositoryPort {

    private static final OrderStatus[] STATUSES = OrderStatus.values();

    private final Map<OrderId, Order> orders = new ConcurrentHashMap<>();

    public InMemoryOrderRepository() {
        Instant now = Instant.now();
        IntStream.rangeClosed(1000, 1059).forEach(n -> {
            var order = new Order(
                    OrderId.of("ORD-" + n),
                    CustomerId.of("CUS-" + (n % 6 + 1)),
                    STATUSES[n % STATUSES.length],
                    BigDecimal.valueOf(15 + (n % 17) * 7.5),
                    "USD",
                    now.minus(1060 - n, ChronoUnit.HOURS));
            orders.put(order.id(), order);
        });
    }

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
}
