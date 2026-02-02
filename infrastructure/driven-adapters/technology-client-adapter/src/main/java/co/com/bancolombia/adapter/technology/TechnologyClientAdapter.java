package co.com.bancolombia.adapter.technology;

import co.com.bancolombia.adapter.technology.dto.TechnologyValidationResponse;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.gateways.TechnologyValidationGateway;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TechnologyClientAdapter implements TechnologyValidationGateway {

    private static final String SERVICE_NAME = "technology";
    private static final String VALIDATE_PATH = "/api/technologies/validate";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    /**
     * Construct adapter with WebClientFactory.
     * The factory will provide a WebClient configured for the "technology" service.
     *
     * @param webClientFactory The generic WebClient factory
     */
    public TechnologyClientAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
    }

    @Override
    public Mono<Boolean> validateTechnologiesExist(List<Long> technologyIds) {
        log.info("Validating technologies: {}", technologyIds);

        String idsParam = buildIdsParameter(technologyIds);

        return performValidationRequest(idsParam)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnSuccess(result -> log.info("Validation result: {}", result))
            .doOnError(error -> log.error("Validation error: {}", error.getMessage()))
            .onErrorResume(this::handleRequestError);
    }

    private String buildIdsParameter(List<Long> technologyIds) {
        return technologyIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    private Mono<Boolean> performValidationRequest(String idsParam) {
        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(VALIDATE_PATH)
                .queryParam("ids", idsParam)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
            .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
            .bodyToMono(TechnologyValidationResponse.class)
            .map(TechnologyValidationResponse::isValid);
    }

    private Mono<Throwable> handleClientError(ClientResponse response) {
        return Mono.error(new BusinessException(
            DomainErrorCode.TECHNOLOGY_VALIDATION_FAILED));
    }

    private Mono<Throwable> handleServerError(ClientResponse response) {
        return Mono.error(new BusinessException(
            DomainErrorCode.TECHNOLOGY_SERVICE_UNAVAILABLE));
    }

    private Mono<Boolean> handleRequestError(Throwable error) {
        if (error instanceof WebClientRequestException) {
            return Mono.error(new BusinessException(
                DomainErrorCode.TECHNOLOGY_SERVICE_UNAVAILABLE));
        }
        return Mono.error(error);
    }
}
