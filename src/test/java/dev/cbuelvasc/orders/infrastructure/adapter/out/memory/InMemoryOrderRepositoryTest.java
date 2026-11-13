package dev.cbuelvasc.orders.infrastructure.adapter.out.memory;

import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import dev.cbuelvasc.orders.infrastructure.adapter.out.OrderRepositoryContract;

class InMemoryOrderRepositoryTest extends OrderRepositoryContract {

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();

    @Override
    protected OrderRepositoryPort repository() {
        return repository;
    }
}
