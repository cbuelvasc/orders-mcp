package dev.cbuelvasc.orders.infrastructure.adapter.in.rest;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.in.CancelOrderUseCase;
import dev.cbuelvasc.orders.domain.port.in.FindOrderUseCase;
import dev.cbuelvasc.orders.domain.port.in.ListRecentOrdersUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Driving adapter for humans. Same use cases as the MCP adapter. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final FindOrderUseCase findOrder;
    private final ListRecentOrdersUseCase listRecentOrders;
    private final CancelOrderUseCase cancelOrder;

    public OrderController(FindOrderUseCase findOrder,
                           ListRecentOrdersUseCase listRecentOrders,
                           CancelOrderUseCase cancelOrder) {
        this.findOrder = findOrder;
        this.listRecentOrders = listRecentOrders;
        this.cancelOrder = cancelOrder;
    }

    @PreAuthorize("hasAuthority('SCOPE_orders:read')")
    @GetMapping("/{orderId}")
    public Order findOrder(@PathVariable String orderId) {
        return findOrder.findById(OrderId.of(orderId));
    }

    @PreAuthorize("hasAuthority('SCOPE_orders:read')")
    @GetMapping
    public List<Order> listRecent(@RequestParam String customerId,
                                  @RequestParam(defaultValue = "5") int limit) {
        return listRecentOrders.listRecent(CustomerId.of(customerId), limit);
    }

    @PreAuthorize("hasAuthority('SCOPE_orders:write')")
    @PostMapping("/{orderId}/cancel")
    public Order cancel(@PathVariable String orderId) {
        return cancelOrder.cancel(OrderId.of(orderId));
    }
}
