package dev.cbuelvasc.orders.infrastructure.adapter.in.mcp;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.in.CancelOrderUseCase;
import dev.cbuelvasc.orders.domain.port.in.FindOrderUseCase;
import dev.cbuelvasc.orders.domain.port.in.ListRecentOrdersUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Driving adapter for AI agents. This is the ONLY class that knows about MCP.
 * It depends on inbound ports, never on the application service or the repository.
 * Every tool declares the OAuth scope it needs: least privilege per tool.
 */
@Component
public class OrderMcpTools {

    static final String READ = "hasAuthority('SCOPE_orders:read')";
    static final String WRITE = "hasAuthority('SCOPE_orders:write')";

    private final FindOrderUseCase findOrder;
    private final ListRecentOrdersUseCase listRecentOrders;
    private final CancelOrderUseCase cancelOrder;
    private final MeterRegistry meterRegistry;

    public OrderMcpTools(FindOrderUseCase findOrder,
                         ListRecentOrdersUseCase listRecentOrders,
                         CancelOrderUseCase cancelOrder,
                         MeterRegistry meterRegistry) {
        this.findOrder = findOrder;
        this.listRecentOrders = listRecentOrders;
        this.cancelOrder = cancelOrder;
        this.meterRegistry = meterRegistry;
    }

    @PreAuthorize(READ)
    @McpTool(name = "findOrder", description = """
            Returns status, total amount and last update of ONE order.
            Use it when the user mentions a specific order id like ORD-1042.
            """)
    public OrderView findOrder(
            @McpToolParam(description = "Order id, format ORD-<digits>, e.g. ORD-1042", required = true)
            String orderId) {
        return observe("findOrder", () -> OrderView.from(findOrder.findById(OrderId.of(orderId))));
    }

    @PreAuthorize(READ)
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

    @PreAuthorize(WRITE)
    @McpTool(name = "cancelOrder", description = """
            Cancels ONE order that has not shipped yet. This action cannot be undone.
            Only call it after the user explicitly confirmed the order id to cancel.
            """)
    public OrderView cancelOrder(
            @McpToolParam(description = "Order id to cancel, format ORD-<digits>", required = true)
            String orderId) {
        return observe("cancelOrder", () -> OrderView.from(cancelOrder.cancel(OrderId.of(orderId))));
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
