package co.com.bancolombia.usecase.getcapacitytechnologycounts;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class GetCapacityTechnologyCountsUseCase {
    private final CapacityRepository capacityRepository;

    public Mono<Map<Long, Long>> execute(List<Long> capacityIds) {
        return Flux.fromIterable(capacityIds)
            .flatMap(capacityId -> capacityRepository.countTechnologiesByCapacityId(capacityId)
                .map(count -> Map.entry(capacityId, count)))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
