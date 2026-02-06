package co.com.bancolombia.usecase.getcapacitycount;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.technology.TechnologyCapacityCountWithRelated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCapacityCountUseCase Tests")
class GetCapacityCountUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    private GetCapacityCountUseCase getCapacityCountUseCase;

    @BeforeEach
    void setUp() {
        getCapacityCountUseCase = new GetCapacityCountUseCase(capacityRepository);
    }

    @Nested
    @DisplayName("execute() - Happy Path")
    class ExecuteHappyPath {

        @Test
        @DisplayName("Should return capacity count with related technologies")
        void shouldReturnCapacityCountWithRelatedTechnologies() {
            // Arrange
            Long technologyId = 1L;
            List<Long> relatedIds = Arrays.asList(2L, 3L, 4L);

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.just(5L));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.fromIterable(relatedIds));

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectNextMatches(result ->
                    result.getCapacityCount() == 5L &&
                    result.getRelatedTechnologyIds().size() == 3 &&
                    result.getRelatedTechnologyIds().containsAll(relatedIds))
                .verifyComplete();

            verify(capacityRepository).countCapacitiesByTechnologyId(technologyId);
            verify(capacityRepository).findRelatedTechnologyIds(technologyId);
        }

        @Test
        @DisplayName("Should handle zero capacity count")
        void shouldHandleZeroCapacityCount() {
            // Arrange
            Long technologyId = 1L;

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.just(0L));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.just(2L, 3L));

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectNextMatches(result ->
                    result.getCapacityCount() == 0L &&
                    result.getRelatedTechnologyIds().size() == 2)
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle empty related technologies")
        void shouldHandleEmptyRelatedTechnologies() {
            // Arrange
            Long technologyId = 1L;

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.just(10L));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.empty());

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectNextMatches(result ->
                    result.getCapacityCount() == 10L &&
                    result.getRelatedTechnologyIds().isEmpty())
                .verifyComplete();

            verify(capacityRepository).countCapacitiesByTechnologyId(technologyId);
            verify(capacityRepository).findRelatedTechnologyIds(technologyId);
        }

        @Test
        @DisplayName("Should correctly build TechnologyCapacityCountWithRelated")
        void shouldCorrectlyBuildTechnologyCapacityCountWithRelated() {
            // Arrange
            Long technologyId = 5L;
            List<Long> relatedIds = Arrays.asList(1L, 2L, 3L, 4L);

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.just(15L));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.fromIterable(relatedIds));

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectNextMatches(result ->
                    result.getCapacityCount() == 15L &&
                    result.getRelatedTechnologyIds().size() == 4 &&
                    result.getRelatedTechnologyIds().containsAll(relatedIds))
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("execute() - Error Handling")
    class ExecuteErrorHandling {

        @Test
        @DisplayName("Should propagate error from countCapacitiesByTechnologyId")
        void shouldPropagateErrorFromCountCapacities() {
            // Arrange
            Long technologyId = 1L;
            RuntimeException dbError = new RuntimeException("Database connection failed");

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.error(dbError));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.just(2L, 3L));

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                    error.getMessage().equals("Database connection failed"))
                .verify();

            verify(capacityRepository).countCapacitiesByTechnologyId(technologyId);
        }

        @Test
        @DisplayName("Should propagate error from findRelatedTechnologyIds")
        void shouldPropagateErrorFromFindRelatedTechnologies() {
            // Arrange
            Long technologyId = 1L;

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.just(5L));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.error(new RuntimeException("Repository error")));

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectError(RuntimeException.class)
                .verify();

            verify(capacityRepository).countCapacitiesByTechnologyId(technologyId);
            verify(capacityRepository).findRelatedTechnologyIds(technologyId);
        }

        @Test
        @DisplayName("Should handle both streams completing with data")
        void shouldHandleBothStreamsCompletingWithData() {
            // Arrange
            Long technologyId = 10L;

            when(capacityRepository.countCapacitiesByTechnologyId(technologyId))
                .thenReturn(Mono.just(20L));
            when(capacityRepository.findRelatedTechnologyIds(technologyId))
                .thenReturn(Flux.just(1L, 2L, 3L, 4L, 5L));

            // Act & Assert
            StepVerifier.create(getCapacityCountUseCase.execute(technologyId))
                .expectNextMatches(result ->
                    result.getCapacityCount() == 20L &&
                    result.getRelatedTechnologyIds().size() == 5)
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("TechnologyCapacityCountWithRelated Model Tests")
    class ModelTests {

        @Test
        @DisplayName("Should create instance with builder")
        void shouldCreateInstanceWithBuilder() {
            // Act
            TechnologyCapacityCountWithRelated result = TechnologyCapacityCountWithRelated.builder()
                .capacityCount(10L)
                .relatedTechnologyIds(Arrays.asList(1L, 2L, 3L))
                .build();

            // Assert
            assertThat(result.getCapacityCount()).isEqualTo(10L);
            assertThat(result.getRelatedTechnologyIds()).hasSize(3).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("Should handle null related technology IDs")
        void shouldHandleNullRelatedTechnologyIds() {
            // Act
            TechnologyCapacityCountWithRelated result = TechnologyCapacityCountWithRelated.builder()
                .capacityCount(5L)
                .relatedTechnologyIds(null)
                .build();

            // Assert
            assertThat(result.getCapacityCount()).isEqualTo(5L);
            assertThat(result.getRelatedTechnologyIds()).isNull();
        }

        @Test
        @DisplayName("Should handle empty related technology IDs list")
        void shouldHandleEmptyRelatedTechnologyIdsList() {
            // Act
            TechnologyCapacityCountWithRelated result = TechnologyCapacityCountWithRelated.builder()
                .capacityCount(15L)
                .relatedTechnologyIds(Collections.emptyList())
                .build();

            // Assert
            assertThat(result.getCapacityCount()).isEqualTo(15L);
            assertThat(result.getRelatedTechnologyIds()).isEmpty();
        }
    }
}
