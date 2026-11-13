package dev.cbuelvasc.orders.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @param resource             canonical URL of this MCP server, e.g. http://localhost:8080/mcp
 * @param authorizationServers issuers that can mint tokens for it
 * @param scopes               scopes this server understands
 */
@ConfigurationProperties("orders.mcp.security")
public record McpSecurityProperties(String resource,
                                    List<String> authorizationServers,
                                    List<String> scopes) {

    public McpSecurityProperties {
        authorizationServers = authorizationServers == null ? List.of() : List.copyOf(authorizationServers);
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
    }

    /** RFC 9728: metadata lives under /.well-known/oauth-protected-resource + the resource path. */
    String resourceMetadataUrl() {
        var uri = java.net.URI.create(resource);
        String origin = uri.getScheme() + "://" + uri.getAuthority();
        return origin + "/.well-known/oauth-protected-resource" + uri.getPath();
    }
}
