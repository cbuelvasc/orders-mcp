package dev.cbuelvasc.orders.infrastructure.config;

import dev.cbuelvasc.orders.application.CancelOrderService;
import dev.cbuelvasc.orders.application.OrderQueryService;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Wires the framework-agnostic core into Spring. */
@Configuration(proxyBeanMethods = false)
class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OrderQueryService orderQueryService(OrderRepositoryPort orderRepository) {
        return new OrderQueryService(orderRepository);
    }

    @Bean
    CancelOrderService cancelOrderService(OrderRepositoryPort orderRepository, Clock clock) {
        return new CancelOrderService(orderRepository, clock);
    }
}
