package co.com.bancolombia.usecase.getcapacitytechnologycounts;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCapacityTechnologyCountsUseCase Tests")
class GetCapacityTechnologyCountsUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    private GetCapacityTechnologyCountsUseCase getCapacityTechnologyCountsUseCase;

    @BeforeEach
    void setUp() {
        getCapacityTechnologyCountsUseCase = new GetCapacityTechnologyCountsUseCase(capacityRepository);
    }

    @Nested
    @DisplayName("execute() - Happy Path")
    class ExecuteHappyPath {

        @Test
        @DisplayName("Should return technology counts for multiple capacity IDs")
        void shouldReturnTechnologyCountsForMultipleCapacityIds() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L, 2L, 3L);

            when(capacityRepository.countTechnologiesByCapacityId(1L))
                .thenReturn(Mono.just(5L));
            when(capacityRepository.countTechnologiesByCapacityId(2L))
                .thenReturn(Mono.just(3L));
            when(capacityRepository.countTechnologiesByCapacityId(3L))
                .thenReturn(Mono.just(7L));

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectNextMatches(result ->
                    result.size() == 3 &&
                    result.get(1L) == 5L &&
                    result.get(2L) == 3L &&
                    result.get(3L) == 7L)
                .verifyComplete();

            verify(capacityRepository).countTechnologiesByCapacityId(1L);
            verify(capacityRepository).countTechnologiesByCapacityId(2L);
            verify(capacityRepository).countTechnologiesByCapacityId(3L);
        }

        @Test
        @DisplayName("Should return technology count for single capacity ID")
        void shouldReturnTechnologyCountForSingleCapacityId() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L);

            when(capacityRepository.countTechnologiesByCapacityId(1L))
                .thenReturn(Mono.just(10L));

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectNextMatches(result ->
                    result.size() == 1 &&
                    result.get(1L) == 10L)
                .verifyComplete();

            verify(capacityRepository).countTechnologiesByCapacityId(1L);
        }

        @Test
        @DisplayName("Should handle zero technology counts")
        void shouldHandleZeroTechnologyCounts() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L, 2L);

            when(capacityRepository.countTechnologiesByCapacityId(1L))
                .thenReturn(Mono.just(0L));
            when(capacityRepository.countTechnologiesByCapacityId(2L))
                .thenReturn(Mono.just(5L));

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectNextMatches(result ->
                    result.size() == 2 &&
                    result.get(1L) == 0L &&
                    result.get(2L) == 5L)
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle large list of capacity IDs")
        void shouldHandleLargeListOfCapacityIds() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

            for (long id : capacityIds) {
                when(capacityRepository.countTechnologiesByCapacityId(id))
                    .thenReturn(Mono.just(id * 2));
            }

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectNextMatches(result ->
                    result.size() == 5 &&
                    result.get(1L) == 2L &&
                    result.get(5L) == 10L)
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("execute() - Edge Cases")
    class ExecuteEdgeCases {

        @Test
        @DisplayName("Should return empty map for empty list")
        void shouldReturnEmptyMapForEmptyList() {
            // Arrange
            List<Long> emptyList = Collections.emptyList();

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(emptyList))
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle maximum technology counts")
        void shouldHandleMaximumTechnologyCounts() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L);

            when(capacityRepository.countTechnologiesByCapacityId(1L))
                .thenReturn(Mono.just(999999L));

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectNextMatches(result ->
                    result.size() == 1 &&
                    result.get(1L) == 999999L)
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("execute() - Error Handling")
    class ExecuteErrorHandling {

        @Test
        @DisplayName("Should propagate repository errors")
        void shouldPropagateRepositoryErrors() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L, 2L);

            when(capacityRepository.countTechnologiesByCapacityId(1L))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectError(RuntimeException.class)
                .verify();
        }

        @Test
        @DisplayName("Should handle error after successful counts")
        void shouldHandleErrorAfterSuccessfulCounts() {
            // Arrange
            List<Long> capacityIds = Arrays.asList(1L, 2L, 3L);

            when(capacityRepository.countTechnologiesByCapacityId(1L))
                .thenReturn(Mono.just(5L));
            when(capacityRepository.countTechnologiesByCapacityId(2L))
                .thenReturn(Mono.error(new RuntimeException("DB connection lost")));

            // Act & Assert
            StepVerifier.create(getCapacityTechnologyCountsUseCase.execute(capacityIds))
                .expectError(RuntimeException.class)
                .verify();
        }
    }
}
