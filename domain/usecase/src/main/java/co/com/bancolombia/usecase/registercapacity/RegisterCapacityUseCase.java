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
        try {
            CapacityValidator.validateName(capacity.getName());
            CapacityValidator.validateDescription(capacity.getDescription());
            CapacityValidator.validateTechnologyIds(capacity.getTechnologyIds());
        } catch (BusinessException e) {
            return Mono.error(e);
        }

        return capacityRepository.existsByName(capacity.getName())
            .flatMap(exists -> {
                if (Boolean.TRUE.equals(exists)) {
                    return Mono.error(new BusinessException(
                        DomainErrorCode.CAPACITY_NAME_ALREADY_EXISTS));
                }
                return technologyValidationGateway
                        .validateTechnologiesExist(capacity.getTechnologyIds());
            })
            .flatMap(isValid -> {
                if (Boolean.FALSE.equals(isValid)) {
                    return Mono.error(new BusinessException(
                        DomainErrorCode.TECHNOLOGIES_NOT_FOUND));
                }
                return capacityRepository.save(capacity);
            });
    }
}
