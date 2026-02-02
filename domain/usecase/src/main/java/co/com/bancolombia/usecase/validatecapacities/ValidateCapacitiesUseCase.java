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

        return ValidationResult.builder()
            .allExist(notFoundIds.isEmpty())
            .existingIds(List.copyOf(existingIds))
            .notFoundIds(notFoundIds)
            .build();
    }

    public static class ValidationResult {
        private final Boolean allExist;
        private final List<Long> existingIds;
        private final List<Long> notFoundIds;

        private ValidationResult(Boolean allExist, List<Long> existingIds, List<Long> notFoundIds) {
            this.allExist = allExist;
            this.existingIds = existingIds;
            this.notFoundIds = notFoundIds;
        }

        public static ValidationResult empty() {
            return new ValidationResult(true, List.of(), List.of());
        }

        public static Builder builder() {
            return new Builder();
        }

        public Boolean getAllExist() {
            return allExist;
        }

        public List<Long> getExistingIds() {
            return existingIds;
        }

        public List<Long> getNotFoundIds() {
            return notFoundIds;
        }

        public static class Builder {
            private Boolean allExist;
            private List<Long> existingIds;
            private List<Long> notFoundIds;

            public Builder allExist(Boolean allExist) {
                this.allExist = allExist;
                return this;
            }

            public Builder existingIds(List<Long> existingIds) {
                this.existingIds = existingIds;
                return this;
            }

            public Builder notFoundIds(List<Long> notFoundIds) {
                this.notFoundIds = notFoundIds;
                return this;
            }

            public ValidationResult build() {
                return new ValidationResult(allExist, existingIds, notFoundIds);
            }
        }
    }
}
