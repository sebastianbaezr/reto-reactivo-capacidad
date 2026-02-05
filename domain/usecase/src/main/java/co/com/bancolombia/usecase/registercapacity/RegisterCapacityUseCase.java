package co.com.bancolombia.usecase.registercapacity;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.gateways.TechnologyValidationGateway;
import co.com.bancolombia.usecase.validator.CapacityValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterCapacityUseCase {
    private final CapacityRepository capacityRepository;
    private final TechnologyValidationGateway technologyValidationGateway;

    public Mono<Capacity> execute(Capacity capacity) {
        return Mono.defer(() -> {
            CapacityValidator.validateName(capacity.getName());
            CapacityValidator.validateDescription(capacity.getDescription());
            CapacityValidator.validateTechnologyIds(capacity.getTechnologyIds());
            return Mono.just(capacity);
        })
            .flatMap(cap -> capacityRepository.existsByName(cap.getName())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.CAPACITY_NAME_ALREADY_EXISTS)))
                .map(v -> cap))
            .flatMap(cap -> technologyValidationGateway.validateTechnologiesExist(cap.getTechnologyIds())
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGIES_NOT_FOUND)))
                .map(v -> cap))
            .flatMap(capacityRepository::save);
    }
}
