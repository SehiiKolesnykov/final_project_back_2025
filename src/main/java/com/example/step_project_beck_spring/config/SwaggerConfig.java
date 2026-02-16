package com.example.step_project_beck_spring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String baseUrl = "https://widi-rho.vercel.app";

        return new OpenAPI()
                .servers(List.of(
                        new Server().url(baseUrl).description("Production Server")
                ))
                .info(new Info()
                        .title("Step Project Backend API")
                        .version("1.0.0")
                        .description("REST API для соціальної мережі. " +
                                "Swagger UI доступний на: " + baseUrl + "/swagger-ui.html")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}

