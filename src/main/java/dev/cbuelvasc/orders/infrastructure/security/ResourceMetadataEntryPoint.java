package dev.cbuelvasc.orders.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 401 + WWW-Authenticate with resource_metadata, as required by the MCP authorization spec.
 * That header is how an MCP client discovers where to get a token.
 */
class ResourceMetadataEntryPoint implements AuthenticationEntryPoint {

    private final String resourceMetadataUrl;

    ResourceMetadataEntryPoint(String resourceMetadataUrl) {
        this.resourceMetadataUrl = resourceMetadataUrl;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                "Bearer resource_metadata=\"%s\"".formatted(resourceMetadataUrl));
    }
}
