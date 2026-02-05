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
        if (capacityIds == null || capacityIds.isEmpty()) {
            return Mono.just(ValidationResult.empty());
        }

        Set<Long> requestedIds = new HashSet<>(capacityIds);

        return capacityRepository.findExistingIds(capacityIds)
            .collectList()
            .map(HashSet::new)
            .map(existingIds -> buildResult(requestedIds, existingIds));
    }

    private ValidationResult buildResult(Set<Long> requestedIds, Set<Long> existingIds) {
        List<Long> notFoundIds = requestedIds.stream()
            .filter(id -> !existingIds.contains(id))
            .toList();

        return new ValidationResult(
            notFoundIds.isEmpty(),
            List.copyOf(existingIds),
            notFoundIds
        );
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
