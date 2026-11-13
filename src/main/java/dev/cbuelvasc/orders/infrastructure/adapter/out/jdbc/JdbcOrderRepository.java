package dev.cbuelvasc.orders.infrastructure.adapter.out.jdbc;

import dev.cbuelvasc.orders.domain.model.CustomerId;
import dev.cbuelvasc.orders.domain.model.Order;
import dev.cbuelvasc.orders.domain.model.OrderId;
import dev.cbuelvasc.orders.domain.model.OrderStatus;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Driven adapter for PostgreSQL. Plain SQL with JdbcClient: no ORM entities leaking into the domain.
 * The mapping between rows and the Order aggregate lives here and only here.
 */
@Repository
public class JdbcOrderRepository implements OrderRepositoryPort {

    private static final String COLUMNS = "id, customer_id, status, total, currency, updated_at";

    private final JdbcClient jdbc;

    public JdbcOrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM orders WHERE id = :id")
                .param("id", id.value())
                .query(JdbcOrderRepository::toOrder)
                .optional();
    }

    @Override
    public List<Order> findRecentByCustomer(CustomerId customerId, int limit) {
        return jdbc.sql("""
                        SELECT %s FROM orders
                        WHERE customer_id = :customerId
                        ORDER BY updated_at DESC
                        LIMIT :limit
                        """.formatted(COLUMNS))
                .param("customerId", customerId.value())
                .param("limit", limit)
                .query(JdbcOrderRepository::toOrder)
                .list();
    }

    @Override
    public Order save(Order order) {
        jdbc.sql("""
                        INSERT INTO orders (id, customer_id, status, total, currency, updated_at)
                        VALUES (:id, :customerId, :status, :total, :currency, :updatedAt)
                        ON CONFLICT (id) DO UPDATE
                        SET status = EXCLUDED.status, total = EXCLUDED.total, updated_at = EXCLUDED.updated_at
                        """)
                .param("id", order.id().value())
                .param("customerId", order.customerId().value())
                .param("status", order.status().name())
                .param("total", order.total())
                .param("currency", order.currency())
                .param("updatedAt", Timestamp.from(order.updatedAt()))
                .update();
        return order;
    }

    private static Order toOrder(ResultSet rs, int rowNum) throws SQLException {
        return new Order(
                OrderId.of(rs.getString("id")),
                CustomerId.of(rs.getString("customer_id")),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("total"),
                rs.getString("currency"),
                rs.getTimestamp("updated_at").toInstant());
    }
}
