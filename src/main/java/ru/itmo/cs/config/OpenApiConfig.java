package ru.itmo.cs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Authentication API")
                .version("1.0.0")
                .description("API для работы с аутентификацией пользователей, включающая регистрацию и вход."));
    }
}

