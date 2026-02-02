package co.com.bancolombia.adapter.technology;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.TechnologySummary;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TechnologyDetailsClientAdapter implements TechnologyRepository {

    private static final String SERVICE_NAME = "technology";
    private static final String GET_TECHNOLOGIES_PATH = "/api/technologies";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClientFactory webClientFactory;

    public TechnologyDetailsClientAdapter(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    @Override
    public Flux<TechnologySummary> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        String idsParam = ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        return fetchTechnologiesFromService(idsParam);
    }

    private Flux<TechnologySummary> fetchTechnologiesFromService(String idsParam) {
        log.info("Fetching technologies from service with ids: {}", idsParam);

        var webClient = webClientFactory.getWebClient(SERVICE_NAME);

        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(GET_TECHNOLOGIES_PATH)
                .queryParam("ids", idsParam)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
            .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
            .bodyToFlux(TechnologySummary.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnNext(tech -> log.debug("Processing technology: {}", tech.getName()))
            .doOnComplete(() -> log.info("Successfully fetched all technologies"))
            .onErrorResume(error -> handleRequestError(error));
    }

    private Mono<Throwable> handleClientError(ClientResponse response) {
        log.error("Client error from technology service");
        return Mono.error(new BusinessException(
            DomainErrorCode.TECHNOLOGY_VALIDATION_FAILED));
    }

    private Mono<Throwable> handleServerError(ClientResponse response) {
        log.error("Server error from technology service");
        return Mono.error(new BusinessException(
            DomainErrorCode.TECHNOLOGY_SERVICE_UNAVAILABLE));
    }

    private Flux<TechnologySummary> handleRequestError(Throwable error) {
        if (error instanceof WebClientRequestException) {
            log.error("Request error connecting to technology service: {}", error.getMessage());
            return Flux.error(new BusinessException(
                DomainErrorCode.TECHNOLOGY_SERVICE_UNAVAILABLE));
        }
        log.error("Error fetching technologies: {}", error.getMessage());
        return Flux.error(error);
    }
}
