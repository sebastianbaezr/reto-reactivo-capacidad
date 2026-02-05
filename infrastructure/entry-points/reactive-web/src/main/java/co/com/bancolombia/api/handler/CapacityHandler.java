package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.CapacityRequest;
import co.com.bancolombia.api.dto.request.ListCapacitiesRequest;
import co.com.bancolombia.api.dto.request.DeleteCapacitiesBatchRequest;
import co.com.bancolombia.api.dto.request.RestoreCapacitiesBatchRequest;
import co.com.bancolombia.api.dto.response.CapacityValidationResponse;
import co.com.bancolombia.api.dto.response.DeleteBatchResponse;
import co.com.bancolombia.api.dto.response.RestoreBatchResponse;
import co.com.bancolombia.api.mapper.CapacityMapper;
import co.com.bancolombia.api.mapper.CapacityListMapper;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.usecase.registercapacity.RegisterCapacityUseCase;
import co.com.bancolombia.usecase.listcapacities.ListCapacitiesUseCase;
import co.com.bancolombia.usecase.validatecapacities.ValidateCapacitiesUseCase;
import co.com.bancolombia.usecase.getcapacity.GetCapacityWithTechnologiesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapacityHandler {

    private final RegisterCapacityUseCase registerCapacityUseCase;
    private final ListCapacitiesUseCase listCapacitiesUseCase;
    private final ValidateCapacitiesUseCase validateCapacitiesUseCase;
    private final GetCapacityWithTechnologiesUseCase getCapacityWithTechnologiesUseCase;
    private final CapacityRepository capacityRepository;
    private final CapacityMapper capacityMapper;
    private final CapacityListMapper capacityListMapper;

    public Mono<ServerResponse> registerCapacity(ServerRequest request) {
        return request.bodyToMono(CapacityRequest.class)
            .map(capacityMapper::toDomain)
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

    public Mono<ServerResponse> validateCapacities(ServerRequest request) {
        return extractCapacityIds(request)
            .flatMap(validateCapacitiesUseCase::execute)
            .map(result -> CapacityValidationResponse.builder()
                .allExist(result.allExist())
                .existingIds(result.existingIds())
                .notFoundIds(result.notFoundIds())
                .build())
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacities validated successfully"))
            .doOnError(e -> log.error("Error validating capacities", e));
    }

    public Mono<ServerResponse> getCapacitiesByIds(ServerRequest request) {
        return extractCapacityIds(request)
            .flatMapMany(capacityRepository::findCapacitiesByIdsWithTechnologies)
            .map(capacityListMapper::toSimpleWithTechnologiesResponse)
            .collectList()
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacities fetched successfully by ids"))
            .doOnError(e -> log.error("Error fetching capacities by ids", e));
    }

    private Mono<List<Long>> extractCapacityIds(ServerRequest request) {
        return Mono.fromCallable(() -> request.queryParam("ids")
            .map(ids -> Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList())
            .orElse(List.of()));
    }

    private Mono<ListCapacitiesRequest> extractQueryParams(ServerRequest request) {
        return Mono.fromCallable(() -> {
            ListCapacitiesRequest req = ListCapacitiesRequest.builder()
                .page(request.queryParam("page").map(Integer::parseInt).orElse(0))
                .size(request.queryParam("size").map(Integer::parseInt).orElse(10))
                .sortBy(request.queryParam("sortBy").orElse("name"))
                .sortOrder(request.queryParam("sortOrder").orElse("asc"))
                .build();
            validateListCapacitiesRequest(req);
            return req;
        });
    }

    private void validateListCapacitiesRequest(ListCapacitiesRequest request) {
        if (request.getPage() < 0) {
            throw new BusinessException(DomainErrorCode.INVALID_PAGE_NUMBER);
        }
        if (request.getSize() < 1 || request.getSize() > 50) {
            throw new BusinessException(DomainErrorCode.INVALID_PAGE_SIZE);
        }
    }

    public Mono<ServerResponse> deleteCapacitiesBatch(ServerRequest request) {
        return request.bodyToMono(DeleteCapacitiesBatchRequest.class)
            .flatMapMany(req -> Flux.fromIterable(req.getCapacityIds())
                .flatMap(capacityRepository::softDeleteCapacity))
            .collectList()
            .map(deletedIds -> DeleteBatchResponse.builder()
                .deletedCount(deletedIds.size())
                .capacitiesDeleted(deletedIds)
                .build())
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacities deleted successfully"))
            .doOnError(e -> log.error("Error deleting capacities", e));
    }

    public Mono<ServerResponse> restoreCapacitiesBatch(ServerRequest request) {
        return request.bodyToMono(RestoreCapacitiesBatchRequest.class)
            .flatMapMany(req -> Flux.fromIterable(req.getCapacityIds())
                .flatMap(capacityRepository::restoreCapacity))
            .collectList()
            .map(restoredIds -> RestoreBatchResponse.builder()
                .restoredCount(restoredIds.size())
                .capacitiesRestored(restoredIds)
                .build())
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacities restored successfully"))
            .doOnError(e -> log.error("Error restoring capacities", e));
    }

    public Mono<ServerResponse> getCapacityWithTechnologies(ServerRequest request) {
        Long capacityId = Long.parseLong(request.pathVariable("capacityId"));
        return getCapacityWithTechnologiesUseCase.execute(capacityId)
            .map(capacityListMapper::toSimpleWithTechnologiesResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity with technologies retrieved successfully"))
            .doOnError(e -> log.error("Error retrieving capacity with technologies", e));
    }
}
