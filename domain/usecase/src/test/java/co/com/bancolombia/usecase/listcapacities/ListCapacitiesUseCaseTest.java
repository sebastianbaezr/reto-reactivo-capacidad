package co.com.bancolombia.usecase.listcapacities;

import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCapacitiesUseCase Tests")
class ListCapacitiesUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    private ListCapacitiesUseCase listCapacitiesUseCase;

    @BeforeEach
    void setUp() {
        listCapacitiesUseCase = new ListCapacitiesUseCase(capacityRepository);
    }

    @Test
    @DisplayName("Should return paginated capacities with default parameters")
    void testExecute_Success() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, "name", SortDirection.ASC);
        Page<CapacityWithTechnologies> expectedPage = createPageResponse(3, 0, 30);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.just(expectedPage));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectNextMatches(page -> page.getCurrentPage() == 0 && page.getPageSize() == 10)
            .verifyComplete();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty page when no capacities exist")
    void testExecute_EmptyPage() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, "name", SortDirection.ASC);
        Page<CapacityWithTechnologies> emptyPage = createPageResponse(0, 0, 0);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.just(emptyPage));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectNextMatches(page -> page.getContent().isEmpty())
            .verifyComplete();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should accept maximum page size of 50")
    void testExecute_MaxPageSize() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 50, "name", SortDirection.ASC);
        Page<CapacityWithTechnologies> expectedPage = createPageResponse(50, 0, 150);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.just(expectedPage));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectNextMatches(page -> page != null)
            .verifyComplete();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should allow sorting by name")
    void testExecute_SortByName() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, "name", SortDirection.ASC);
        Page<CapacityWithTechnologies> expectedPage = createPageResponse(3, 0, 3);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.just(expectedPage));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectNextMatches(page -> page != null)
            .verifyComplete();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should allow sorting by technology count")
    void testExecute_SortByTechnologyCount() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, "technologyCount", SortDirection.DESC);
        Page<CapacityWithTechnologies> expectedPage = createPageResponse(3, 0, 3);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.just(expectedPage));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectNextMatches(page -> page != null)
            .verifyComplete();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should allow null sort field")
    void testExecute_NullSortBy() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, null, SortDirection.ASC);
        Page<CapacityWithTechnologies> expectedPage = createPageResponse(3, 0, 3);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.just(expectedPage));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectNextMatches(page -> page != null)
            .verifyComplete();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when page number is negative")
    void testExecute_InvalidPageNumber_Negative() {
        // Arrange
        PageRequest pageRequest = createPageRequest(-1, 10, "name", SortDirection.ASC);

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.INVALID_PAGE_NUMBER.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when page size is zero")
    void testExecute_InvalidPageSize_Zero() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 0, "name", SortDirection.ASC);

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.INVALID_PAGE_SIZE.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when page size exceeds maximum of 50")
    void testExecute_InvalidPageSize_TooLarge() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 51, "name", SortDirection.ASC);

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.INVALID_PAGE_SIZE.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when sort field is invalid")
    void testExecute_InvalidSortField() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, "invalidField", SortDirection.ASC);

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.INVALID_SORT_FIELD.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should propagate repository errors")
    void testExecute_RepositoryError() {
        // Arrange
        PageRequest pageRequest = createPageRequest(0, 10, "name", SortDirection.ASC);

        when(capacityRepository.findAllWithPagination(any(PageRequest.class)))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(listCapacitiesUseCase.execute(pageRequest))
            .expectErrorMatches(error -> error instanceof RuntimeException &&
                error.getMessage().equals("Database error"))
            .verify();

        verify(capacityRepository).findAllWithPagination(any(PageRequest.class));
    }

    // Helper methods
    private PageRequest createPageRequest(int page, int size, String sortBy, SortDirection sortDirection) {
        return PageRequest.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();
    }

    private Page<CapacityWithTechnologies> createPageResponse(int contentSize, int currentPage, int totalElements) {
        List<CapacityWithTechnologies> content = new ArrayList<>();
        for (int i = 0; i < contentSize; i++) {
            CapacityWithTechnologies capacity = new CapacityWithTechnologies();
            capacity.setId((long) (i + 1));
            capacity.setName("Capacity " + (i + 1));
            content.add(capacity);
        }

        return Page.<CapacityWithTechnologies>builder()
            .content(content)
            .currentPage(currentPage)
            .totalPages((totalElements + 9) / 10)
            .totalElements(totalElements)
            .pageSize(10)
            .hasNextPage(currentPage < ((totalElements + 9) / 10 - 1))
            .hasPreviousPage(currentPage > 0)
            .sortBy("name")
            .sortDirection(SortDirection.ASC)
            .build();
    }
}
