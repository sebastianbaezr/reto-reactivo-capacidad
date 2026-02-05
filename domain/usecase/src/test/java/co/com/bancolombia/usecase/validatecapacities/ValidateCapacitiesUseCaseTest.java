package co.com.bancolombia.usecase.validatecapacities;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateCapacitiesUseCase Tests")
class ValidateCapacitiesUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    private ValidateCapacitiesUseCase validateCapacitiesUseCase;

    @BeforeEach
    void setUp() {
        validateCapacitiesUseCase = new ValidateCapacitiesUseCase(capacityRepository);
    }

    @Test
    @DisplayName("Should return allExist=true when all IDs exist")
    void testExecute_AllIdsExist() {
        // Arrange
        List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);

        when(capacityRepository.findExistingIds(requestedIds))
            .thenReturn(Flux.just(1L, 2L, 3L));

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
            .expectNextMatches(result ->
                result.allExist() &&
                result.existingIds().size() == 3 &&
                result.notFoundIds().isEmpty())
            .verifyComplete();

        verify(capacityRepository).findExistingIds(requestedIds);
    }

    @Test
    @DisplayName("Should return allExist=false when some IDs not found")
    void testExecute_SomeIdsNotFound() {
        // Arrange
        List<Long> requestedIds = Arrays.asList(1L, 2L, 3L, 4L);

        when(capacityRepository.findExistingIds(requestedIds))
            .thenReturn(Flux.just(1L, 3L));

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
            .expectNextMatches(result ->
                !result.allExist() &&
                result.existingIds().size() == 2 &&
                result.notFoundIds().size() == 2 &&
                result.notFoundIds().containsAll(Arrays.asList(2L, 4L)))
            .verifyComplete();

        verify(capacityRepository).findExistingIds(requestedIds);
    }

    @Test
    @DisplayName("Should return allExist=false when no IDs found")
    void testExecute_NoIdsFound() {
        // Arrange
        List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);

        when(capacityRepository.findExistingIds(requestedIds))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
            .expectNextMatches(result ->
                !result.allExist() &&
                result.existingIds().isEmpty() &&
                result.notFoundIds().size() == 3 &&
                result.notFoundIds().containsAll(requestedIds))
            .verifyComplete();

        verify(capacityRepository).findExistingIds(requestedIds);
    }

    @Test
    @DisplayName("Should return empty ValidationResult when list is empty")
    void testExecute_EmptyList() {
        // Arrange
        List<Long> emptyList = Collections.emptyList();

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(emptyList))
            .expectNextMatches(result ->
                result.allExist() &&
                result.existingIds().isEmpty() &&
                result.notFoundIds().isEmpty())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty ValidationResult when list is null")
    void testExecute_NullList() {
        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(null))
            .expectNextMatches(result ->
                result.allExist() &&
                result.existingIds().isEmpty() &&
                result.notFoundIds().isEmpty())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle single ID validation")
    void testExecute_SingleId() {
        // Arrange
        List<Long> singleId = Arrays.asList(1L);

        when(capacityRepository.findExistingIds(singleId))
            .thenReturn(Flux.just(1L));

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(singleId))
            .expectNextMatches(result ->
                result.allExist() &&
                result.existingIds().size() == 1 &&
                result.existingIds().contains(1L))
            .verifyComplete();

        verify(capacityRepository).findExistingIds(singleId);
    }

    @Test
    @DisplayName("Should handle single ID not found")
    void testExecute_SingleIdNotFound() {
        // Arrange
        List<Long> singleId = Arrays.asList(1L);

        when(capacityRepository.findExistingIds(singleId))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(singleId))
            .expectNextMatches(result ->
                !result.allExist() &&
                result.notFoundIds().size() == 1 &&
                result.notFoundIds().contains(1L))
            .verifyComplete();

        verify(capacityRepository).findExistingIds(singleId);
    }

    @Test
    @DisplayName("Should deduplicate IDs using Set")
    void testExecute_DuplicateIds() {
        // Arrange
        List<Long> duplicateIds = Arrays.asList(1L, 2L, 3L, 1L, 2L);

        when(capacityRepository.findExistingIds(duplicateIds))
            .thenReturn(Flux.just(1L, 2L));

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(duplicateIds))
            .expectNextMatches(result ->
                !result.allExist() &&
                result.existingIds().size() == 2 &&
                result.notFoundIds().size() == 1 &&
                result.notFoundIds().contains(3L))
            .verifyComplete();

        verify(capacityRepository).findExistingIds(duplicateIds);
    }

    @Test
    @DisplayName("Should propagate repository errors")
    void testExecute_RepositoryError() {
        // Arrange
        List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);

        when(capacityRepository.findExistingIds(requestedIds))
            .thenReturn(Flux.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
            .expectErrorMatches(error -> error instanceof RuntimeException &&
                error.getMessage().equals("Database error"))
            .verify();

        verify(capacityRepository).findExistingIds(requestedIds);
    }

    @Test
    @DisplayName("Should validate ValidationResult builder and getters")
    void testExecute_ValidationResultBuilder() {
        // Act
        ValidateCapacitiesUseCase.ValidationResult result = new ValidateCapacitiesUseCase.ValidationResult(
            true,
            Arrays.asList(1L, 2L, 3L),
            Collections.emptyList());

        // Assert - Direct assertion since no Mono involved
        org.assertj.core.api.Assertions.assertThat(result.allExist()).isTrue();
        org.assertj.core.api.Assertions.assertThat(result.existingIds()).hasSize(3);
        org.assertj.core.api.Assertions.assertThat(result.notFoundIds()).isEmpty();
    }
}
