package com.klaraapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Klara")
                        .description("Feito com ❤ para quem quer <strong>clareza nas finanças")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Adiel Amaral")
                                .email("adielnfs@gmail.com")));
    }
}