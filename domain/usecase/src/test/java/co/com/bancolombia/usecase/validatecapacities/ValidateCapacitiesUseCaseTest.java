package co.com.bancolombia.usecase.validatecapacities;

import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
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

    @Nested
    @DisplayName("execute() - Happy Path Scenarios")
    class ExecuteHappyPath {

        @Test
        @DisplayName("Should return allExist=true when all IDs exist")
        void shouldReturnAllExistTrueWhenAllIdsExist() {
            // Arrange
            List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L, 3L))))
                .thenReturn(Flux.just(1L, 2L, 3L));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
                .expectNextMatches(result ->
                    result.allExist() &&
                    result.existingIds().size() == 3 &&
                    result.notFoundIds().isEmpty())
                .verifyComplete();

            verify(capacityRepository).findExistingIds(argThat(containsElements(1L, 2L, 3L)));
        }

        @Test
        @DisplayName("Should return allExist=false when some IDs not found")
        void shouldReturnAllExistFalseWhenSomeIdsNotFound() {
            // Arrange
            List<Long> requestedIds = Arrays.asList(1L, 2L, 3L, 4L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L, 3L, 4L))))
                .thenReturn(Flux.just(1L, 3L));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
                .expectNextMatches(result ->
                    !result.allExist() &&
                    result.existingIds().size() == 2 &&
                    result.notFoundIds().size() == 2 &&
                    result.notFoundIds().containsAll(Arrays.asList(2L, 4L)))
                .verifyComplete();

            verify(capacityRepository).findExistingIds(argThat(containsElements(1L, 2L, 3L, 4L)));
        }

        @Test
        @DisplayName("Should handle single ID validation successfully")
        void shouldHandleSingleIdValidationSuccessfully() {
            // Arrange
            List<Long> singleId = Arrays.asList(1L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L))))
                .thenReturn(Flux.just(1L));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(singleId))
                .expectNextMatches(result ->
                    result.allExist() &&
                    result.existingIds().size() == 1 &&
                    result.existingIds().contains(1L) &&
                    result.notFoundIds().isEmpty())
                .verifyComplete();

            verify(capacityRepository).findExistingIds(argThat(containsElements(1L)));
        }

        @Test
        @DisplayName("Should correctly filter out non-found IDs from large list")
        void shouldFilterOutNonFoundIdsFromLargeList() {
            // Arrange
            List<Long> requestedIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 10L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L, 3L, 4L, 5L, 10L))))
                .thenReturn(Flux.just(1L, 3L, 5L));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
                .expectNextMatches(result ->
                    !result.allExist() &&
                    result.existingIds().size() == 3 &&
                    result.existingIds().containsAll(Arrays.asList(1L, 3L, 5L)) &&
                    result.notFoundIds().size() == 3 &&
                    result.notFoundIds().containsAll(Arrays.asList(2L, 4L, 10L)))
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("execute() - Edge Cases")
    class ExecuteEdgeCases {

        @Test
        @DisplayName("Should return empty ValidationResult when list is empty")
        void shouldReturnEmptyValidationResultWhenListIsEmpty() {
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
        void shouldReturnEmptyValidationResultWhenListIsNull() {
            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(null))
                .expectNextMatches(result ->
                    result.allExist() &&
                    result.existingIds().isEmpty() &&
                    result.notFoundIds().isEmpty())
                .verifyComplete();
        }

        @Test
        @DisplayName("Should return allExist=false when no IDs found")
        void shouldReturnAllExistFalseWhenNoIdsFound() {
            // Arrange
            List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L, 3L))))
                .thenReturn(Flux.empty());

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
                .expectNextMatches(result ->
                    !result.allExist() &&
                    result.existingIds().isEmpty() &&
                    result.notFoundIds().size() == 3 &&
                    result.notFoundIds().containsAll(requestedIds))
                .verifyComplete();

            verify(capacityRepository).findExistingIds(argThat(containsElements(1L, 2L, 3L)));
        }

        @Test
        @DisplayName("Should handle single ID not found")
        void shouldHandleSingleIdNotFound() {
            // Arrange
            List<Long> singleId = Arrays.asList(1L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L))))
                .thenReturn(Flux.empty());

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(singleId))
                .expectNextMatches(result ->
                    !result.allExist() &&
                    result.existingIds().isEmpty() &&
                    result.notFoundIds().size() == 1 &&
                    result.notFoundIds().contains(1L))
                .verifyComplete();

            verify(capacityRepository).findExistingIds(argThat(containsElements(1L)));
        }

        @Test
        @DisplayName("Should deduplicate IDs from input list")
        void shouldDeduplicateIdsFromInputList() {
            // Arrange
            List<Long> duplicateIds = Arrays.asList(1L, 2L, 3L, 1L, 2L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L, 3L))))
                .thenReturn(Flux.just(1L, 2L));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(duplicateIds))
                .expectNextMatches(result ->
                    !result.allExist() &&
                    result.existingIds().size() == 2 &&
                    result.existingIds().containsAll(Arrays.asList(1L, 2L)) &&
                    result.notFoundIds().size() == 1 &&
                    result.notFoundIds().contains(3L))
                .verifyComplete();
        }
    }

    @Nested
    @DisplayName("execute() - Error Handling")
    class ExecuteErrorHandling {

        @Test
        @DisplayName("Should propagate repository errors correctly")
        void shouldPropagateRepositoryErrorsCorrectly() {
            // Arrange
            List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);
            RuntimeException databaseError = new RuntimeException("Database connection failed");

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L, 3L))))
                .thenReturn(Flux.error(databaseError));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                    error.getMessage().equals("Database connection failed"))
                .verify();

            verify(capacityRepository).findExistingIds(argThat(containsElements(1L, 2L, 3L)));
        }

        @Test
        @DisplayName("Should handle illegal argument exception from repository")
        void shouldHandleIllegalArgumentExceptionFromRepository() {
            // Arrange
            List<Long> requestedIds = Arrays.asList(1L, 2L);

            when(capacityRepository.findExistingIds(argThat(containsElements(1L, 2L))))
                .thenReturn(Flux.error(new IllegalArgumentException("Invalid input")));

            // Act & Assert
            StepVerifier.create(validateCapacitiesUseCase.execute(requestedIds))
                .expectError(IllegalArgumentException.class)
                .verify();
        }
    }

    @Nested
    @DisplayName("ValidationResult - Record Tests")
    class ValidationResultTests {

        @Test
        @DisplayName("Should create ValidationResult with all parameters")
        void shouldCreateValidationResultWithAllParameters() {
            // Act
            ValidateCapacitiesUseCase.ValidationResult result =
                new ValidateCapacitiesUseCase.ValidationResult(
                    true,
                    Arrays.asList(1L, 2L, 3L),
                    Collections.emptyList()
                );

            // Assert
            assertThat(result.allExist()).isTrue();
            assertThat(result.existingIds()).hasSize(3).containsExactlyInAnyOrder(1L, 2L, 3L);
            assertThat(result.notFoundIds()).isEmpty();
        }

        @Test
        @DisplayName("Should create ValidationResult with empty collections")
        void shouldCreateValidationResultWithEmptyCollections() {
            // Act
            ValidateCapacitiesUseCase.ValidationResult result =
                new ValidateCapacitiesUseCase.ValidationResult(
                    false,
                    Collections.emptyList(),
                    Collections.emptyList()
                );

            // Assert
            assertThat(result.allExist()).isFalse();
            assertThat(result.existingIds()).isEmpty();
            assertThat(result.notFoundIds()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty ValidationResult from static factory method")
        void shouldReturnEmptyValidationResultFromStaticFactoryMethod() {
            // Act
            ValidateCapacitiesUseCase.ValidationResult emptyResult =
                ValidateCapacitiesUseCase.ValidationResult.empty();

            // Assert
            assertThat(emptyResult.allExist()).isTrue();
            assertThat(emptyResult.existingIds()).isEmpty();
            assertThat(emptyResult.notFoundIds()).isEmpty();
        }

        @Test
        @DisplayName("Should correctly handle ValidationResult with mixed data")
        void shouldCorrectlyHandleValidationResultWithMixedData() {
            // Act
            ValidateCapacitiesUseCase.ValidationResult result =
                new ValidateCapacitiesUseCase.ValidationResult(
                    false,
                    Arrays.asList(1L, 2L),
                    Arrays.asList(3L, 4L, 5L)
                );

            // Assert
            assertThat(result.allExist()).isFalse();
            assertThat(result.existingIds()).hasSize(2).containsExactlyInAnyOrder(1L, 2L);
            assertThat(result.notFoundIds()).hasSize(3).containsExactlyInAnyOrder(3L, 4L, 5L);
        }

        @Test
        @DisplayName("Should validate record equality based on values")
        void shouldValidateRecordEqualityBasedOnValues() {
            // Arrange
            ValidateCapacitiesUseCase.ValidationResult result1 =
                new ValidateCapacitiesUseCase.ValidationResult(
                    true,
                    Arrays.asList(1L, 2L),
                    Collections.emptyList()
                );

            ValidateCapacitiesUseCase.ValidationResult result2 =
                new ValidateCapacitiesUseCase.ValidationResult(
                    true,
                    Arrays.asList(1L, 2L),
                    Collections.emptyList()
                );

            // Assert
            assertThat(result1).isEqualTo(result2);
        }
    }

    // Helper method to match lists containing specific elements
    private ArgumentMatcher<List<Long>> containsElements(Long... elements) {
        return list -> list != null &&
            list.size() == elements.length &&
            list.containsAll(Arrays.asList(elements));
    }
}
