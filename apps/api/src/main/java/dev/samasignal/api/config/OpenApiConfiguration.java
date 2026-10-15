package dev.samasignal.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI apiDocumentation() {
        return new OpenAPI().info(new Info()
                .title("SamaSignal API — socle")
                .version("0.1.0")
                .description("Routes métier non implémentées. Contrat cible : docs/openapi.yaml dans le dépôt. "
                        + "Le healthcheck est disponible sur /actuator/health."));
    }
}
