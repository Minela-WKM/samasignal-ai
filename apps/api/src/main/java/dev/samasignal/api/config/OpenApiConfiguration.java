package dev.samasignal.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI documentationSwagger() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("SamaSignal API")
                                .version("0.1.0")
                                .description(
                                        "API REST publique en français, versionnée sous"
                                            + " /api/v2/signalements. Swagger permet de consulter"
                                            + " et tester les méthodes HTTP. Le healthcheck est"
                                            + " disponible sur /actuator/health."));
    }
}
