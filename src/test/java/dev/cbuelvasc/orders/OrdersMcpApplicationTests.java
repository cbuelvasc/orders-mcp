package dev.cbuelvasc.orders;

import dev.cbuelvasc.orders.infrastructure.adapter.in.mcp.OrderMcpTools;
import dev.cbuelvasc.orders.support.PostgresTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The whole hexagon wired together: Spring AI + Spring Security + PostgreSQL (Testcontainers). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestcontainersConfig.class)
class OrdersMcpApplicationTests {

    @Autowired
    OrderMcpTools tools;

    @Autowired
    Environment env;

    @Test
    @WithMockUser(authorities = "SCOPE_orders:read")
    void aReaderCanCallReadTools() {
        assertThat(tools.findOrder("ORD-1042").orderId()).isEqualTo("ORD-1042");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_orders:read")
    void aReaderCannotCancelOrders() {
        assertThatThrownBy(() -> tools.cancelOrder("ORD-1040"))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    @WithMockUser(authorities = {"SCOPE_orders:read", "SCOPE_orders:write"})
    void aWriterCanCancelAnOrderThatHasNotShipped() {
        // ORD-1040: 1040 % 5 = 0 -> CREATED in the seed data
        assertThat(tools.cancelOrder("ORD-1040").status()).isEqualTo("CANCELLED");
    }

    @Test
    void mcpEndpointRejectsAnonymousCallsAndPointsToTheMetadata() throws Exception {
        var response = http(HttpRequest.newBuilder(uri("/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type", "application/json"));

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("WWW-Authenticate"))
                .hasValueSatisfying(h -> assertThat(h)
                        .startsWith("Bearer resource_metadata=")
                        .contains("/.well-known/oauth-protected-resource/mcp"));
    }

    @Test
    void protectedResourceMetadataIsPublic() throws Exception {
        var response = http(HttpRequest.newBuilder(uri("/.well-known/oauth-protected-resource/mcp")).GET());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("authorization_servers", "orders:read", "orders:write");
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + env.getProperty("local.server.port") + path);
    }

    private static HttpResponse<String> http(HttpRequest.Builder request) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
