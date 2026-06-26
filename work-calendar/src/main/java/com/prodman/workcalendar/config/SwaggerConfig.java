package com.prodman.workcalendar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Work Calendar API")
                        .description("Work Calendar, Employees and Shifts Management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ProdMan Team")
                                .email("support@prodman.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://prodman.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8888/work-calendar")
                                .description("Gateway Server")
                ));
    }
}