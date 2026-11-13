package dev.cbuelvasc.orders;

import dev.cbuelvasc.orders.infrastructure.adapter.in.mcp.OrderMcpTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrdersMcpApplicationTests {

    @Autowired
    OrderMcpTools tools;

    @Test
    void wiresTheWholeHexagon() {
        assertThat(tools.findOrder("ORD-1042").orderId()).isEqualTo("ORD-1042");
    }
}
