package com.pm.librarymanagementsystem.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        Components components =
                new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(
                                                SecurityScheme.Type.HTTP
                                        )
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "JWT access token enviado mediante Authorization: Bearer <token>"
                                        )
                        )
                        .addSecuritySchemes(
                                "cookieAuth",
                                new SecurityScheme()
                                        .type(
                                                SecurityScheme.Type.APIKEY
                                        )
                                        .in(
                                                SecurityScheme.In.COOKIE
                                        )
                                        .name("access_token")
                                        .description(
                                                "JWT access token almacenado en una cookie HttpOnly"
                                        )
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Library Management System API"
                                )
                                .version("1.0")
                                .description("""
                                        REST API para la gestión de una biblioteca.

                                        Autenticación:
                                        - JWT mediante cookie HttpOnly `access_token` para clientes web.
                                        - JWT Bearer también soportado por la API.
                                        - Las operaciones protegidas contra CSRF utilizan
                                          el header `X-XSRF-TOKEN`.
                                        - El token CSRF puede obtenerse mediante
                                          `GET /api/v1/auth/csrf`.
                                        """)
                                .contact(
                                        new Contact()
                                                .name(
                                                        "Gerardo Martínez"
                                                )
                                )
                )
                .components(components)
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("cookieAuth")
                );
    }
}