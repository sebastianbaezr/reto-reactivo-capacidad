package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.CapacityRequest;
import co.com.bancolombia.api.dto.request.ListCapacitiesRequest;
import co.com.bancolombia.api.mapper.CapacityMapper;
import co.com.bancolombia.api.mapper.CapacityListMapper;
import co.com.bancolombia.usecase.registercapacity.RegisterCapacityUseCase;
import co.com.bancolombia.usecase.listcapacities.ListCapacitiesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapacityHandler {

    private final RegisterCapacityUseCase registerCapacityUseCase;
    private final ListCapacitiesUseCase listCapacitiesUseCase;
    private final CapacityMapper capacityMapper;
    private final CapacityListMapper capacityListMapper;

    public Mono<ServerResponse> registerCapacity(ServerRequest request) {
        return request.bodyToMono(CapacityRequest.class)
            .map(capacityMapper::toEntity)
            .flatMap(registerCapacityUseCase::execute)
            .map(capacityMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity registered successfully"))
            .doOnError(e -> log.error("Error registering capacity", e));
    }

    public Mono<ServerResponse> listCapacities(ServerRequest request) {
        return extractQueryParams(request)
            .flatMap(listRequest -> listCapacitiesUseCase.execute(capacityListMapper.toPageRequest(listRequest)))
            .map(capacityListMapper::toPageResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacities listed successfully"))
            .doOnError(e -> log.error("Error listing capacities", e));
    }

    private Mono<ListCapacitiesRequest> extractQueryParams(ServerRequest request) {
        return Mono.fromCallable(() -> ListCapacitiesRequest.builder()
            .page(request.queryParam("page").map(Integer::parseInt).orElse(0))
            .size(request.queryParam("size").map(Integer::parseInt).orElse(10))
            .sortBy(request.queryParam("sortBy").orElse("name"))
            .sortOrder(request.queryParam("sortOrder").orElse("asc"))
            .build());
    }
}
