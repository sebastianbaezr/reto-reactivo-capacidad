package co.com.bancolombia.usecase.getcapacity;

import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.TechnologySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCapacityWithTechnologiesUseCase Tests")
class GetCapacityWithTechnologiesUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    private GetCapacityWithTechnologiesUseCase getCapacityWithTechnologiesUseCase;

    @BeforeEach
    void setUp() {
        getCapacityWithTechnologiesUseCase = new GetCapacityWithTechnologiesUseCase(capacityRepository);
    }

    @Nested
    @DisplayName("execute() - Happy Path")
    class ExecuteHappyPath {

        @Test
        @DisplayName("Should return capacity with technologies")
        void shouldReturnCapacityWithTechnologies() {
            // Arrange
            Long capacityId = 1L;
            List<TechnologySummary> technologies = Arrays.asList(
                new TechnologySummary(1L, "Java"),
                new TechnologySummary(2L, "Spring")
            );
            CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
                .id(capacityId)
                .name("Backend")
                .description("Backend development")
                .technologies(technologies)
                .build();

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.just(capacity));

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectNextMatches(result ->
                    result.getId() == capacityId &&
                    result.getName().equals("Backend") &&
                    result.getTechnologies().size() == 2)
                .verifyComplete();

            verify(capacityRepository).findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId)));
        }

        @Test
        @DisplayName("Should return capacity with empty technologies list")
        void shouldReturnCapacityWithEmptyTechnologiesList() {
            // Arrange
            Long capacityId = 2L;
            CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
                .id(capacityId)
                .name("Frontend")
                .description("Frontend development")
                .technologies(java.util.Collections.emptyList())
                .build();

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.just(capacity));

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectNextMatches(result ->
                    result.getId() == capacityId &&
                    result.getName().equals("Frontend") &&
                    result.getTechnologies().isEmpty())
                .verifyComplete();
        }

        @Test
        @DisplayName("Should extract first item from flux")
        void shouldExtractFirstItemFromFlux() {
            // Arrange
            Long capacityId = 1L;
            CapacityWithTechnologies capacity1 = CapacityWithTechnologies.builder()
                .id(capacityId)
                .name("Capacity 1")
                .technologies(Arrays.asList(new TechnologySummary(1L, "Java")))
                .build();

            CapacityWithTechnologies capacity2 = CapacityWithTechnologies.builder()
                .id(2L)
                .name("Capacity 2")
                .technologies(Arrays.asList(new TechnologySummary(2L, "Python")))
                .build();

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.just(capacity1, capacity2));

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectNextMatches(result -> result.getName().equals("Capacity 1"))
                .verifyComplete();
        }

        @Test
        @DisplayName("Should handle capacity with multiple technologies")
        void shouldHandleCapacityWithMultipleTechnologies() {
            // Arrange
            Long capacityId = 3L;
            List<TechnologySummary> technologies = Arrays.asList(
                new TechnologySummary(1L, "Java"),
                new TechnologySummary(2L, "Spring"),
                new TechnologySummary(3L, "PostgreSQL"),
                new TechnologySummary(4L, "Docker")
            );
            CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
                .id(capacityId)
                .name("Full Stack")
                .description("Full stack development")
                .technologies(technologies)
                .build();

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.just(capacity));

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectNextMatches(result ->
                    result.getId() == capacityId &&
                    result.getTechnologies().size() == 4)
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("execute() - Not Found Scenarios")
    class NotFoundScenarios {

        @Test
        @DisplayName("Should throw BusinessException when capacity not found")
        void shouldThrowBusinessExceptionWhenCapacityNotFound() {
            // Arrange
            Long capacityId = 999L;

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.empty());

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectErrorMatches(error -> error instanceof BusinessException &&
                    ((BusinessException) error).getCode().equals(DomainErrorCode.CAPACITY_NOT_FOUND.getCode()))
                .verify();

            verify(capacityRepository).findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId)));
        }

        @Test
        @DisplayName("Should throw correct error code for missing capacity")
        void shouldThrowCorrectErrorCodeForMissingCapacity() {
            // Arrange
            Long capacityId = 100L;

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.empty());

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    BusinessException be = (BusinessException) error;
                    assertThat(be.getCode()).isEqualTo(DomainErrorCode.CAPACITY_NOT_FOUND.getCode());
                })
                .verify();
        }
    }

    @Nested
    @DisplayName("execute() - Error Handling")
    class ExecuteErrorHandling {

        @Test
        @DisplayName("Should propagate repository errors")
        void shouldPropagateRepositoryErrors() {
            // Arrange
            Long capacityId = 1L;
            RuntimeException dbError = new RuntimeException("Database connection failed");

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.error(dbError));

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                    error.getMessage().equals("Database connection failed"))
                .verify();

            verify(capacityRepository).findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId)));
        }

        @Test
        @DisplayName("Should handle data access exception")
        void shouldHandleDataAccessException() {
            // Arrange
            Long capacityId = 1L;

            when(capacityRepository.findCapacitiesByIdsWithTechnologies(argThat(list ->
                list.size() == 1 && list.contains(capacityId))))
                .thenReturn(Flux.error(new IllegalArgumentException("Invalid capacity ID")));

            // Act & Assert
            StepVerifier.create(getCapacityWithTechnologiesUseCase.execute(capacityId))
                .expectError(IllegalArgumentException.class)
                .verify();
        }
    }

    @Nested
    @DisplayName("CapacityWithTechnologies Model Tests")
    class ModelTests {

        @Test
        @DisplayName("Should create CapacityWithTechnologies with builder")
        void shouldCreateCapacityWithTechnologiesWithBuilder() {
            // Arrange
            List<TechnologySummary> technologies = Arrays.asList(
                new TechnologySummary(1L, "Java"),
                new TechnologySummary(2L, "Spring")
            );

            // Act
            CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
                .id(1L)
                .name("Backend")
                .description("Backend development capacity")
                .technologies(technologies)
                .build();

            // Assert
            assertThat(capacity.getId()).isEqualTo(1L);
            assertThat(capacity.getName()).isEqualTo("Backend");
            assertThat(capacity.getDescription()).isEqualTo("Backend development capacity");
            assertThat(capacity.getTechnologies()).hasSize(2);
        }

        @Test
        @DisplayName("Should handle audit fields in model")
        void shouldHandleAuditFieldsInModel() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<TechnologySummary> technologies = Arrays.asList(
                new TechnologySummary(1L, "Java")
            );

            // Act
            CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
                .id(1L)
                .name("Test Capacity")
                .technologies(technologies)
                .build();

            capacity.setCreatedAt(now);
            capacity.setUpdatedAt(now);

            // Assert
            assertThat(capacity.getCreatedAt()).isNotNull();
            assertThat(capacity.getUpdatedAt()).isNotNull();
        }
    }
}
