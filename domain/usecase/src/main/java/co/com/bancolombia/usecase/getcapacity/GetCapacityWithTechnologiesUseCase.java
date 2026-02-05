package co.com.bancolombia.usecase.getcapacity;

import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class GetCapacityWithTechnologiesUseCase {
    private final CapacityRepository capacityRepository;

    public Mono<CapacityWithTechnologies> execute(Long capacityId) {
        return capacityRepository.findCapacitiesByIdsWithTechnologies(List.of(capacityId))
            .next()
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.CAPACITY_NOT_FOUND)));
    }
}
