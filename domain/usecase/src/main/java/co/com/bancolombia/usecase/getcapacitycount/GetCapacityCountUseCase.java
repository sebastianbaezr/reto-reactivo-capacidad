package co.com.bancolombia.usecase.getcapacitycount;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.technology.TechnologyCapacityCountWithRelated;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetCapacityCountUseCase {
    private final CapacityRepository capacityRepository;

    public Mono<TechnologyCapacityCountWithRelated> execute(Long technologyId) {
        return Mono.zip(
            capacityRepository.countCapacitiesByTechnologyId(technologyId),
            capacityRepository.findRelatedTechnologyIds(technologyId).collectList(),
            (count, relatedIds) -> TechnologyCapacityCountWithRelated.builder()
                .capacityCount(count)
                .relatedTechnologyIds(relatedIds)
                .build()
        );
    }
}
