package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.TechnologyCapacityCountsRequest;
import co.com.bancolombia.api.dto.response.TechnologyCapacityCountResponse;
import co.com.bancolombia.api.dto.response.TechnologyCapacityCountsResponse;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TechnologyHandler {

    private final CapacityRepository capacityRepository;

    public Mono<ServerResponse> getCapacityCount(ServerRequest request) {
        Long technologyId = Long.parseLong(request.pathVariable("technologyId"));
        return capacityRepository.countCapacitiesByTechnologyId(technologyId)
            .map(count -> TechnologyCapacityCountResponse.builder()
                .technologyId(technologyId)
                .capacityCount(count)
                .build())
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity count retrieved for technology {}", technologyId))
            .doOnError(e -> log.error("Error getting capacity count for technology {}", technologyId, e));
    }

    public Mono<ServerResponse> getCapacityCounts(ServerRequest request) {
        return request.bodyToMono(TechnologyCapacityCountsRequest.class)
            .flatMapMany(req -> Flux.fromIterable(req.getTechnologyIds())
                .flatMap(technologyId -> capacityRepository.countCapacitiesByTechnologyId(technologyId)
                    .map(count -> Map.entry(String.valueOf(technologyId), count))))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
            .map(counts -> TechnologyCapacityCountsResponse.builder()
                .technologyCounts(counts)
                .build())
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Capacity counts retrieved for technologies"))
            .doOnError(e -> log.error("Error getting capacity counts for technologies", e));
    }
}
