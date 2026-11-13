package dev.cbuelvasc.orders.infrastructure.adapter.in.mcp;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.in.FindOrderUseCase;
import dev.cbuelvasc.orders.domain.port.in.ListRecentOrdersUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Driving adapter for AI agents. This is the ONLY class that knows about MCP.
 * It depends on inbound ports, never on the application service or the repository.
 */
@Component
public class OrderMcpTools {

    private final FindOrderUseCase findOrder;
    private final ListRecentOrdersUseCase listRecentOrders;
    private final MeterRegistry meterRegistry;

    public OrderMcpTools(FindOrderUseCase findOrder,
                         ListRecentOrdersUseCase listRecentOrders,
                         MeterRegistry meterRegistry) {
        this.findOrder = findOrder;
        this.listRecentOrders = listRecentOrders;
        this.meterRegistry = meterRegistry;
    }

    @McpTool(name = "findOrder", description = """
            Returns status, total amount and last update of ONE order.
            Use it when the user mentions a specific order id like ORD-1042.
            """)
    public OrderView findOrder(
            @McpToolParam(description = "Order id, format ORD-<digits>, e.g. ORD-1042", required = true)
            String orderId) {
        return observe("findOrder", () -> OrderView.from(findOrder.findById(OrderId.of(orderId))));
    }

    @McpTool(name = "listRecentOrders", description = """
            Lists the most recent orders of ONE customer, newest first.
            Use it when the user asks about the latest orders of a customer.
            """)
    public List<OrderView> listRecentOrders(
            @McpToolParam(description = "Customer id, format CUS-<digits>, e.g. CUS-7", required = true)
            String customerId,
            @McpToolParam(description = "How many orders to return, from 1 to 20", required = false)
            Integer limit) {
        int requested = limit == null ? 5 : limit;
        return observe("listRecentOrders", () -> listRecentOrders
                .listRecent(CustomerId.of(customerId), requested)
                .stream()
                .map(OrderView::from)
                .toList());
    }

    /** Timer + error counter per tool -> Prometheus -> Grafana. */
    private <T> T observe(String tool, Supplier<T> call) {
        try {
            return meterRegistry.timer("mcp.tool.duration", "tool", tool).record(call);
        } catch (RuntimeException e) {
            meterRegistry.counter("mcp.tool.errors", "tool", tool,
                    "exception", e.getClass().getSimpleName()).increment();
            throw e; // Spring AI turns it into an MCP error result the model can read
        }
    }
}
