package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.TechnologyCapacityCountsRequest;
import co.com.bancolombia.api.mapper.TechnologyMapper;
import co.com.bancolombia.usecase.getcapacitycount.GetCapacityCountUseCase;
import co.com.bancolombia.usecase.getcapacitycounts.GetCapacityCountsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TechnologyHandler {

    private final GetCapacityCountUseCase getCapacityCountUseCase;
    private final GetCapacityCountsUseCase getCapacityCountsUseCase;
    private final TechnologyMapper technologyMapper;

    public Mono<ServerResponse> getCapacityCount(ServerRequest request) {
        Long technologyId = Long.parseLong(request.pathVariable("technologyId"));
        return getCapacityCountUseCase.execute(technologyId)
            .map(countWithRelated -> technologyMapper.toTechnologyCapacityCountResponse(technologyId, countWithRelated))
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity count retrieved for technology {}", technologyId))
            .doOnError(e -> log.error("Error getting capacity count for technology {}", technologyId, e));
    }

    public Mono<ServerResponse> getCapacityCounts(ServerRequest request) {
        return request.bodyToMono(TechnologyCapacityCountsRequest.class)
            .flatMap(req -> getCapacityCountsUseCase.execute(req.getTechnologyIds()))
            .map(technologyMapper::toTechnologyCapacityCountsResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity counts retrieved for technologies"))
            .doOnError(e -> log.error("Error getting capacity counts for technologies", e));
    }
}
