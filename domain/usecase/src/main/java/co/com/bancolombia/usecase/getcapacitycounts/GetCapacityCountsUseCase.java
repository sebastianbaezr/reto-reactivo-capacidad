package co.com.bancolombia.usecase.getcapacitycounts;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class GetCapacityCountsUseCase {
    private final CapacityRepository capacityRepository;

    public Mono<Map<Long, Long>> execute(List<Long> technologyIds) {
        return Flux.fromIterable(technologyIds)
            .flatMap(technologyId -> capacityRepository.countCapacitiesByTechnologyId(technologyId)
                .map(count -> Map.entry(technologyId, count)))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
