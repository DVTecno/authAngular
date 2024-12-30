package com.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@OpenAPIDefinition(
        info = @Info(
                title = "Auth API",
                description = "API for managing user authentication and authorization.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Auth Team",
                        url = "https://login-clase.up.railway.app/dashboard",
                        email = "auth.sup@gmail.com"
                ),
                license = @License(
                        name = "Standard Software Use License",
                        url = "https://login-clase.up.railway.app/license"
                )
        ),
        servers = {
                @Server(description = "Development Server", url = "http://localhost:8090"),
                @Server(description = "Production Server", url = "https://login-clase.up.railway.app")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Bearer authentication scheme for accessing the API. A valid JWT token is required.",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("com.auth")
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui/");
        registry
                .addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/openapi/");
    }
}

