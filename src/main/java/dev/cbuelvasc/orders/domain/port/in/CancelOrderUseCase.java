package dev.cbuelvasc.orders.domain.port.in;

import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;

/** Inbound port with side effects: adapters must protect it with stronger permissions. */
public interface CancelOrderUseCase {

    /**
     * @throws dev.cbuelvasc.orders.domain.exception.OrderNotFoundException if it does not exist
     * @throws dev.cbuelvasc.orders.domain.exception.OrderCannotBeCancelledException if it already shipped
     */
    Order cancel(OrderId id);
}
