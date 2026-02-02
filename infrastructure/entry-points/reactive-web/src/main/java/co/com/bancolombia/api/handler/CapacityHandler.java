package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.CapacityRequest;
import co.com.bancolombia.api.dto.response.ApiResponseData;
import co.com.bancolombia.api.dto.response.CapacityResponse;
import co.com.bancolombia.api.mapper.CapacityMapper;
import co.com.bancolombia.usecase.registercapacity.RegisterCapacityUseCase;
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
    private final CapacityMapper capacityMapper;

    public Mono<ServerResponse> registerCapacity(ServerRequest request) {
        return request.bodyToMono(CapacityRequest.class)
            .map(capacityMapper::toEntity)
            .flatMap(registerCapacityUseCase::execute)
            .map(capacity -> { // deseo hacer el map en 1 sola linea sin lambda
                CapacityResponse response = capacityMapper.toResponse(capacity);
                return response;
            })
            .map(ApiResponseData::of)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity registered successfully"))
            .onErrorResume(e -> {
                log.error("Error registering capacity", e);
                return Mono.error(e);
            });
    }
}
