package dev.cbuelvasc.orders.domain.port.in;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;

import java.util.List;

public interface ListRecentOrdersUseCase {

    int MAX_LIMIT = 20;

    /** Newest first. The limit is clamped to [1, MAX_LIMIT] by the core. */
    List<Order> listRecent(CustomerId customerId, int limit);
}
