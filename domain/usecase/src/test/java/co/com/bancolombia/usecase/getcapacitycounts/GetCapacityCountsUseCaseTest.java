package co.com.bancolombia.usecase.getcapacitycounts;

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
@DisplayName("GetCapacityCountsUseCase Tests")
class GetCapacityCountsUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    private GetCapacityCountsUseCase getCapacityCountsUseCase;

    @BeforeEach
    void setUp() {
        getCapacityCountsUseCase = new GetCapacityCountsUseCase(capacityRepository);
    }

    @Nested
    @DisplayName("execute() - Happy Path")
    class ExecuteHappyPath {

        @Test
        @DisplayName("Should return capacity counts for multiple technology IDs")
        void shouldReturnCapacityCountsForMultipleTechnologyIds() {
            // Arrange
            List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);

            when(capacityRepository.countCapacitiesByTechnologyId(1L))
                .thenReturn(Mono.just(5L));
            when(capacityRepository.countCapacitiesByTechnologyId(2L))
                .thenReturn(Mono.just(3L));
            when(capacityRepository.countCapacitiesByTechnologyId(3L))
                .thenReturn(Mono.just(7L));

            // Act & Assert
            StepVerifier.create(getCapacityCountsUseCase.execute(technologyIds))
                .expectNextMatches(result ->
                    result.size() == 3 &&
                    result.get(1L) == 5L &&
                    result.get(2L) == 3L &&
                    result.get(3L) == 7L)
                .verifyComplete();

            verify(capacityRepository).countCapacitiesByTechnologyId(1L);
            verify(capacityRepository).countCapacitiesByTechnologyId(2L);
            verify(capacityRepository).countCapacitiesByTechnologyId(3L);
        }

        @Test
        @DisplayName("Should return capacity count for single technology ID")
        void shouldReturnCapacityCountForSingleTechnologyId() {
            // Arrange
            List<Long> technologyIds = Arrays.asList(1L);

            when(capacityRepository.countCapacitiesByTechnologyId(1L))
                .thenReturn(Mono.just(10L));

            // Act & Assert
            StepVerifier.create(getCapacityCountsUseCase.execute(technologyIds))
                .expectNextMatches(result ->
                    result.size() == 1 &&
                    result.get(1L) == 10L)
                .verifyComplete();

            verify(capacityRepository).countCapacitiesByTechnologyId(1L);
        }

        @Test
        @DisplayName("Should handle zero capacity counts")
        void shouldHandleZeroCapacityCounts() {
            // Arrange
            List<Long> technologyIds = Arrays.asList(1L, 2L);

            when(capacityRepository.countCapacitiesByTechnologyId(1L))
                .thenReturn(Mono.just(0L));
            when(capacityRepository.countCapacitiesByTechnologyId(2L))
                .thenReturn(Mono.just(5L));

            // Act & Assert
            StepVerifier.create(getCapacityCountsUseCase.execute(technologyIds))
                .expectNextMatches(result ->
                    result.size() == 2 &&
                    result.get(1L) == 0L &&
                    result.get(2L) == 5L)
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
            StepVerifier.create(getCapacityCountsUseCase.execute(emptyList))
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle large capacity counts")
        void shouldHandleLargeCapacityCounts() {
            // Arrange
            List<Long> technologyIds = Arrays.asList(1L);

            when(capacityRepository.countCapacitiesByTechnologyId(1L))
                .thenReturn(Mono.just(1000000L));

            // Act & Assert
            StepVerifier.create(getCapacityCountsUseCase.execute(technologyIds))
                .expectNextMatches(result ->
                    result.size() == 1 &&
                    result.get(1L) == 1000000L)
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
            List<Long> technologyIds = Arrays.asList(1L, 2L);

            when(capacityRepository.countCapacitiesByTechnologyId(1L))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

            // Act & Assert
            StepVerifier.create(getCapacityCountsUseCase.execute(technologyIds))
                .expectError(RuntimeException.class)
                .verify();
        }

        @Test
        @DisplayName("Should handle partial failures in flux")
        void shouldHandlePartialFailuresInFlux() {
            // Arrange
            List<Long> technologyIds = Arrays.asList(1L, 2L, 3L);

            when(capacityRepository.countCapacitiesByTechnologyId(1L))
                .thenReturn(Mono.just(5L));
            when(capacityRepository.countCapacitiesByTechnologyId(2L))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

            // Act & Assert
            StepVerifier.create(getCapacityCountsUseCase.execute(technologyIds))
                .expectError(RuntimeException.class)
                .verify();
        }
    }
}
