package co.com.bancolombia.api.config;

import co.com.bancolombia.api.Handler;
import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.technology.gateways.TechnologyValidationGateway;
import co.com.bancolombia.usecase.registercapacity.RegisterCapacityUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {ConfigTest.TestConfiguration.class, RouterRest.class, Handler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Configuration
    static class TestConfiguration {
        @Bean
        public CapacityRepository capacityRepository() {
            return mock(CapacityRepository.class);
        }

        @Bean
        public TechnologyValidationGateway technologyValidationGateway() {
            return mock(TechnologyValidationGateway.class);
        }

        @Bean
        public RegisterCapacityUseCase registerCapacityUseCase(
            CapacityRepository capacityRepository,
            TechnologyValidationGateway technologyValidationGateway) {
            return new RegisterCapacityUseCase(capacityRepository, technologyValidationGateway);
        }
    }

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/api/usecase/path")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}