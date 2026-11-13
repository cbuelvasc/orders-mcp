package dev.cbuelvasc.orders.domain.port.in;

import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;

/** Inbound port: driving adapters (MCP, REST) depend on this, never on the implementation. */
public interface FindOrderUseCase {

    /** @throws dev.cbuelvasc.orders.domain.exception.OrderNotFoundException if it does not exist */
    Order findById(OrderId id);
}
