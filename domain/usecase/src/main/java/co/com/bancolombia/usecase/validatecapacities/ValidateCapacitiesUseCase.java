package co.com.bancolombia.usecase.validatecapacities;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ValidateCapacitiesUseCase {
    private final CapacityRepository capacityRepository;

    public Mono<ValidationResult> execute(List<Long> capacityIds) {
        return Mono.justOrEmpty(capacityIds)
            .filter(ids -> !ids.isEmpty())
            .map(Set::copyOf)
            .flatMap(this::validateExistenceAndBuildResult)
            .switchIfEmpty(Mono.just(ValidationResult.empty()));
    }

    private Mono<ValidationResult> validateExistenceAndBuildResult(Set<Long> requestedIds) {
        return capacityRepository.findExistingIds(requestedIds.stream().toList())
            .collectList()
            .map(HashSet::new)
            .map(existingIds -> buildValidationResult(requestedIds, existingIds));
    }

    private ValidationResult buildValidationResult(Set<Long> requestedIds, Set<Long> existingIds) {
        List<Long> notFoundIds = findNotFoundIds(requestedIds, existingIds);
        return new ValidationResult(
            notFoundIds.isEmpty(),
            List.copyOf(existingIds),
            notFoundIds
        );
    }

    private List<Long> findNotFoundIds(Set<Long> requestedIds, Set<Long> existingIds) {
        return requestedIds.stream()
            .filter(id -> !existingIds.contains(id))
            .toList();
    }

    public record ValidationResult(
        Boolean allExist,
        List<Long> existingIds,
        List<Long> notFoundIds
    ) {
        public static ValidationResult empty() {
            return new ValidationResult(true, List.of(), List.of());
        }
    }
}
