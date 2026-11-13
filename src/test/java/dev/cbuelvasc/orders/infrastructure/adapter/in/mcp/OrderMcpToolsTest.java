package dev.cbuelvasc.orders.infrastructure.adapter.in.mcp;

import dev.cbuelvasc.orders.domain.exception.OrderNotFoundException;
import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderMcpToolsTest {

    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();

    private final OrderMcpTools tools = new OrderMcpTools(
            id -> {
                if (id.value().equals("ORD-1042")) {
                    return order(id.value());
                }
                throw new OrderNotFoundException(id);
            },
            (customerId, limit) -> List.of(order("ORD-2"), order("ORD-1")),
            meters);

    @Test
    void findOrderReturnsAModelFriendlyView() {
        var view = tools.findOrder("ord-1042");

        assertThat(view.orderId()).isEqualTo("ORD-1042");
        assertThat(view.status()).isEqualTo("SHIPPED");
        assertThat(view.isFinal()).isFalse();
        assertThat(meters.timer("mcp.tool.duration", "tool", "findOrder").count()).isEqualTo(1);
    }

    @Test
    void errorsAreReadableByTheModelAndCounted() {
        assertThatThrownBy(() -> tools.findOrder("ORD-9"))
                .hasMessage("Order not found: ORD-9");

        assertThat(meters.counter("mcp.tool.errors", "tool", "findOrder",
                "exception", "OrderNotFoundException").count()).isEqualTo(1);
    }

    @Test
    void listRecentOrdersUsesADefaultLimit() {
        var views = tools.listRecentOrders("CUS-1", null);

        assertThat(views).extracting(OrderView::orderId).containsExactly("ORD-2", "ORD-1");
    }

    private static Order order(String id) {
        return new Order(OrderId.of(id), CustomerId.of("CUS-1"), OrderStatus.SHIPPED,
                new BigDecimal("120.50"), "USD", Instant.parse("2026-11-13T10:00:00Z"));
    }
}
