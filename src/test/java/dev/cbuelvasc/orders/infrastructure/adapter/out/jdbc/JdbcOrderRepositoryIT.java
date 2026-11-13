package dev.cbuelvasc.orders.infrastructure.adapter.out.jdbc;

import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import dev.cbuelvasc.orders.infrastructure.adapter.out.OrderRepositoryContract;
import dev.cbuelvasc.orders.support.PostgresTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/** Same contract as the in-memory fake, now against real PostgreSQL + Flyway migrations. */
@SpringBootTest
@Import(PostgresTestcontainersConfig.class)
class JdbcOrderRepositoryIT extends OrderRepositoryContract {

    @Autowired
    JdbcOrderRepository repository;

    @Override
    protected OrderRepositoryPort repository() {
        return repository;
    }

    @Test
    void flywaySeedsTheDemoData() {
        assertThat(repository.findById(OrderId.of("ORD-1042"))).isPresent();
    }
}
