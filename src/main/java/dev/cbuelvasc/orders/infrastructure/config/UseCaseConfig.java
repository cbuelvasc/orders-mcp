package dev.cbuelvasc.orders.infrastructure.config;

import dev.cbuelvasc.orders.application.OrderQueryService;
import dev.cbuelvasc.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the framework-agnostic core into Spring. */
@Configuration(proxyBeanMethods = false)
class UseCaseConfig {

    @Bean
    OrderQueryService orderQueryService(OrderRepositoryPort orderRepository) {
        return new OrderQueryService(orderRepository);
    }
}
