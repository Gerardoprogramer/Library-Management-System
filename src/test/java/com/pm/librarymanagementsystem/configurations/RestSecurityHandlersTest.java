package com.pm.librarymanagementsystem.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RestSecurityHandlersTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    void authenticationEntryPointShouldReturnUnauthorizedJson()
            throws Exception {

        RestAuthenticationEntryPoint entryPoint =
                new RestAuthenticationEntryPoint(
                        objectMapper
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException(
                        "Invalid token"
                )
        );

        assertEquals(
                401,
                response.getStatus()
        );

        JsonNode body =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertFalse(
                body.get("success").asBoolean()
        );

        assertEquals(
                "Autenticación requerida",
                body.get("message").asText()
        );
    }

    @Test
    void accessDeniedHandlerShouldReturnForbiddenJson()
            throws Exception {

        RestAccessDeniedHandler handler =
                new RestAccessDeniedHandler(
                        objectMapper
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException(
                        "Forbidden"
                )
        );

        assertEquals(
                403,
                response.getStatus()
        );

        JsonNode body =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertFalse(
                body.get("success").asBoolean()
        );

        assertEquals(
                "Acceso denegado",
                body.get("message").asText()
        );
    }
}