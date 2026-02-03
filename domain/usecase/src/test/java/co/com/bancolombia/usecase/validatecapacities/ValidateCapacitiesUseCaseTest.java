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
                result.getAllExist() &&
                result.getExistingIds().size() == 3 &&
                result.getNotFoundIds().isEmpty())
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
                !result.getAllExist() &&
                result.getExistingIds().size() == 2 &&
                result.getNotFoundIds().size() == 2 &&
                result.getNotFoundIds().containsAll(Arrays.asList(2L, 4L)))
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
                !result.getAllExist() &&
                result.getExistingIds().isEmpty() &&
                result.getNotFoundIds().size() == 3 &&
                result.getNotFoundIds().containsAll(requestedIds))
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
                result.getAllExist() &&
                result.getExistingIds().isEmpty() &&
                result.getNotFoundIds().isEmpty())
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty ValidationResult when list is null")
    void testExecute_NullList() {
        // Act & Assert
        StepVerifier.create(validateCapacitiesUseCase.execute(null))
            .expectNextMatches(result ->
                result.getAllExist() &&
                result.getExistingIds().isEmpty() &&
                result.getNotFoundIds().isEmpty())
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
                result.getAllExist() &&
                result.getExistingIds().size() == 1 &&
                result.getExistingIds().contains(1L))
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
                !result.getAllExist() &&
                result.getNotFoundIds().size() == 1 &&
                result.getNotFoundIds().contains(1L))
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
                !result.getAllExist() &&
                result.getExistingIds().size() == 2 &&
                result.getNotFoundIds().size() == 1 &&
                result.getNotFoundIds().contains(3L))
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
        ValidateCapacitiesUseCase.ValidationResult result = ValidateCapacitiesUseCase.ValidationResult.builder()
            .allExist(true)
            .existingIds(Arrays.asList(1L, 2L, 3L))
            .notFoundIds(Arrays.asList())
            .build();

        // Assert - Direct assertion since no Mono involved
        org.assertj.core.api.Assertions.assertThat(result.getAllExist()).isTrue();
        org.assertj.core.api.Assertions.assertThat(result.getExistingIds()).hasSize(3);
        org.assertj.core.api.Assertions.assertThat(result.getNotFoundIds()).isEmpty();
    }
}
