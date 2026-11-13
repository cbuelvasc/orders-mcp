package dev.cbuelvasc.orders.infrastructure.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** RFC 9728 OAuth 2.0 Protected Resource Metadata: "who protects me and with which scopes". */
@RestController
class ProtectedResourceMetadataController {

    private final McpSecurityProperties props;

    ProtectedResourceMetadataController(McpSecurityProperties props) {
        this.props = props;
    }

    @GetMapping({"/.well-known/oauth-protected-resource", "/.well-known/oauth-protected-resource/mcp"})
    Map<String, Object> metadata() {
        return Map.of(
                "resource", props.resource(),
                "authorization_servers", props.authorizationServers(),
                "scopes_supported", props.scopes(),
                "bearer_methods_supported", List.of("header"));
    }
}
