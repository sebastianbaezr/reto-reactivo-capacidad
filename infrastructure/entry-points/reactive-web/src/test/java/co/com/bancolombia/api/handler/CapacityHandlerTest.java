package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.CapacityRequest;
import co.com.bancolombia.api.dto.request.ListCapacitiesRequest;
import co.com.bancolombia.api.dto.response.CapacityResponse;
import co.com.bancolombia.api.dto.response.CapacityValidationResponse;
import co.com.bancolombia.api.dto.response.CapacityWithTechnologiesSimpleResponse;
import co.com.bancolombia.api.mapper.CapacityListMapper;
import co.com.bancolombia.api.mapper.CapacityMapper;
import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.usecase.listcapacities.ListCapacitiesUseCase;
import co.com.bancolombia.usecase.registercapacity.RegisterCapacityUseCase;
import co.com.bancolombia.usecase.validatecapacities.ValidateCapacitiesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CapacityHandler Tests")
class CapacityHandlerTest {

    @Mock
    private RegisterCapacityUseCase registerCapacityUseCase;

    @Mock
    private ListCapacitiesUseCase listCapacitiesUseCase;

    @Mock
    private ValidateCapacitiesUseCase validateCapacitiesUseCase;

    @Mock
    private CapacityRepository capacityRepository;

    @Mock
    private CapacityMapper capacityMapper;

    @Mock
    private CapacityListMapper capacityListMapper;

    @Mock
    private ServerRequest serverRequest;

    private CapacityHandler capacityHandler;

    @BeforeEach
    void setUp() {
        capacityHandler = new CapacityHandler(
            registerCapacityUseCase,
            listCapacitiesUseCase,
            validateCapacitiesUseCase,
            capacityRepository,
            capacityMapper,
            capacityListMapper
        );
    }

    // ===== registerCapacity Tests (4) =====

    @Test
    @DisplayName("Should register capacity successfully and return 201 Created")
    void testRegisterCapacity_Success() {
        // Arrange
        CapacityRequest request = CapacityRequest.builder()
            .name("Backend")
            .description("Backend capabilities")
            .technologyIds(Arrays.asList(1L, 2L, 3L))
            .build();

        Capacity capacity = Capacity.builder().name("Backend").build();
        Capacity savedCapacity = Capacity.builder().id(1L).name("Backend").build();
        CapacityResponse response = CapacityResponse.builder().id(1L).name("Backend").build();

        when(serverRequest.bodyToMono(CapacityRequest.class))
            .thenReturn(Mono.just(request));
        when(capacityMapper.toDomain(request))
            .thenReturn(capacity);
        when(registerCapacityUseCase.execute(capacity))
            .thenReturn(Mono.just(savedCapacity));
        when(capacityMapper.toResponse(savedCapacity))
            .thenReturn(response);

        // Act & Assert
        StepVerifier.create(capacityHandler.registerCapacity(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 201)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle invalid request during registration")
    void testRegisterCapacity_InvalidRequest() {
        // Arrange
        when(serverRequest.bodyToMono(CapacityRequest.class))
            .thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // Act & Assert
        StepVerifier.create(capacityHandler.registerCapacity(serverRequest))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    @DisplayName("Should propagate BusinessException from use case during registration")
    void testRegisterCapacity_UseCaseError() {
        // Arrange
        CapacityRequest request = CapacityRequest.builder()
            .name("Backend")
            .description("Backend capabilities")
            .technologyIds(Arrays.asList(1L, 2L, 3L))
            .build();

        Capacity capacity = Capacity.builder().name("Backend").build();

        when(serverRequest.bodyToMono(CapacityRequest.class))
            .thenReturn(Mono.just(request));
        when(capacityMapper.toDomain(request))
            .thenReturn(capacity);
        when(registerCapacityUseCase.execute(capacity))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.CAPACITY_NAME_ALREADY_EXISTS)));

        // Act & Assert
        StepVerifier.create(capacityHandler.registerCapacity(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle mapper error during registration")
    void testRegisterCapacity_MapperError() {
        // Arrange
        CapacityRequest request = CapacityRequest.builder()
            .name("Backend")
            .description("Backend capabilities")
            .technologyIds(Arrays.asList(1L, 2L, 3L))
            .build();

        when(serverRequest.bodyToMono(CapacityRequest.class))
            .thenReturn(Mono.just(request));
        when(capacityMapper.toDomain(request))
            .thenThrow(new RuntimeException("Mapping failed"));

        // Act & Assert
        StepVerifier.create(capacityHandler.registerCapacity(serverRequest))
            .expectError(RuntimeException.class)
            .verify();
    }

    // ===== listCapacities Tests (5) =====

    @Test
    @DisplayName("Should list capacities successfully with all query parameters")
    void testListCapacities_Success() {
        // Arrange
        when(serverRequest.queryParam("page")).thenReturn(Optional.of("0"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.of("name"));
        when(serverRequest.queryParam("sortOrder")).thenReturn(Optional.of("asc"));

        Page<CapacityWithTechnologies> page = createEmptyPage(0);

        when(capacityListMapper.toPageRequest(any(ListCapacitiesRequest.class)))
            .thenReturn(createPageRequest(0, 10, "name", SortDirection.ASC));
        when(listCapacitiesUseCase.execute(any()))
            .thenReturn(Mono.just(page));
        when(capacityListMapper.toPageResponse(any()))
            .thenReturn(mock(co.com.bancolombia.api.dto.response.PageResponse.class));

        // Act & Assert
        StepVerifier.create(capacityHandler.listCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should apply default parameters when query params missing")
    void testListCapacities_DefaultParams() {
        // Arrange
        when(serverRequest.queryParam("page")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("size")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("sortOrder")).thenReturn(Optional.empty());

        Page<CapacityWithTechnologies> page = createEmptyPage(0);

        when(capacityListMapper.toPageRequest(any(ListCapacitiesRequest.class)))
            .thenReturn(createPageRequest(0, 10, "name", SortDirection.ASC));
        when(listCapacitiesUseCase.execute(any()))
            .thenReturn(Mono.just(page));
        lenient().when(capacityListMapper.toPageResponse(page))
            .thenReturn(mock(co.com.bancolombia.api.dto.response.PageResponse.class));

        // Act & Assert
        StepVerifier.create(capacityHandler.listCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle integer parameters correctly")
    void testListCapacities_ValidIntegerParam() {
        // Arrange
        when(serverRequest.queryParam("page")).thenReturn(Optional.of("1"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.of("name"));
        when(serverRequest.queryParam("sortOrder")).thenReturn(Optional.of("asc"));

        Page<CapacityWithTechnologies> page = createEmptyPage(1);

        when(capacityListMapper.toPageRequest(any(ListCapacitiesRequest.class)))
            .thenReturn(createPageRequest(1, 10, "name", SortDirection.ASC));
        when(listCapacitiesUseCase.execute(any()))
            .thenReturn(Mono.just(page));
        when(capacityListMapper.toPageResponse(any()))
            .thenReturn(mock(co.com.bancolombia.api.dto.response.PageResponse.class));

        // Act & Assert
        StepVerifier.create(capacityHandler.listCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate BusinessException from use case")
    void testListCapacities_UseCaseError() {
        // Arrange
        when(serverRequest.queryParam("page")).thenReturn(Optional.of("0"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.of("name"));
        when(serverRequest.queryParam("sortOrder")).thenReturn(Optional.of("asc"));

        when(capacityListMapper.toPageRequest(any(ListCapacitiesRequest.class)))
            .thenReturn(createPageRequest(0, 10, "name", SortDirection.ASC));
        when(listCapacitiesUseCase.execute(any()))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.INVALID_PAGE_NUMBER)));

        // Act & Assert
        StepVerifier.create(capacityHandler.listCapacities(serverRequest))
            .expectError(BusinessException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle empty page response")
    void testListCapacities_EmptyPage() {
        // Arrange
        when(serverRequest.queryParam("page")).thenReturn(Optional.of("5"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.of("name"));
        when(serverRequest.queryParam("sortOrder")).thenReturn(Optional.of("asc"));

        Page<CapacityWithTechnologies> emptyPage = createEmptyPage(5);

        when(capacityListMapper.toPageRequest(any(ListCapacitiesRequest.class)))
            .thenReturn(createPageRequest(5, 10, "name", SortDirection.ASC));
        when(listCapacitiesUseCase.execute(any()))
            .thenReturn(Mono.just(emptyPage));
        lenient().when(capacityListMapper.toPageResponse(emptyPage))
            .thenReturn(mock(co.com.bancolombia.api.dto.response.PageResponse.class));

        // Act & Assert
        StepVerifier.create(capacityHandler.listCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    // ===== validateCapacities Tests (4) =====

    @Test
    @DisplayName("Should validate capacities and return result")
    void testValidateCapacities_Success() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1,2,3"));

        ValidateCapacitiesUseCase.ValidationResult result = new ValidateCapacitiesUseCase.ValidationResult(
            true,
            Arrays.asList(1L, 2L, 3L),
            new ArrayList<>());

        when(validateCapacitiesUseCase.execute(any(List.class)))
            .thenReturn(Mono.just(result));

        // Act & Assert
        StepVerifier.create(capacityHandler.validateCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty IDs parameter")
    void testValidateCapacities_EmptyIds() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.empty());

        ValidateCapacitiesUseCase.ValidationResult result = ValidateCapacitiesUseCase.ValidationResult.empty();

        when(validateCapacitiesUseCase.execute(any(List.class)))
            .thenReturn(Mono.just(result));

        // Act & Assert
        StepVerifier.create(capacityHandler.validateCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle invalid IDs format")
    void testValidateCapacities_InvalidFormat() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1,invalid,3"));

        // Act & Assert
        StepVerifier.create(capacityHandler.validateCapacities(serverRequest))
            .expectError(NumberFormatException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle validation result with not found IDs")
    void testValidateCapacities_WithNotFoundIds() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1,2,3,4"));

        ValidateCapacitiesUseCase.ValidationResult result = new ValidateCapacitiesUseCase.ValidationResult(
            false,
            Arrays.asList(1L, 3L),
            Arrays.asList(2L, 4L));

        when(validateCapacitiesUseCase.execute(any(List.class)))
            .thenReturn(Mono.just(result));

        // Act & Assert
        StepVerifier.create(capacityHandler.validateCapacities(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    // ===== getCapacitiesByIds Tests (5) =====

    @Test
    @DisplayName("Should get capacities by IDs successfully")
    void testGetCapacitiesByIds_Success() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1,2,3"));

        CapacityWithTechnologies capacity1 = new CapacityWithTechnologies();
        capacity1.setId(1L);
        CapacityWithTechnologies capacity2 = new CapacityWithTechnologies();
        capacity2.setId(2L);

        when(capacityRepository.findCapacitiesByIdsWithTechnologies(any(List.class)))
            .thenReturn(Flux.just(capacity1, capacity2));
        when(capacityListMapper.toSimpleWithTechnologiesResponse(any()))
            .thenReturn(mock(CapacityWithTechnologiesSimpleResponse.class));

        // Act & Assert
        StepVerifier.create(capacityHandler.getCapacitiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty list when no IDs provided")
    void testGetCapacitiesByIds_EmptyIds() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.empty());

        when(capacityRepository.findCapacitiesByIdsWithTechnologies(any(List.class)))
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(capacityHandler.getCapacitiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get single capacity by ID")
    void testGetCapacitiesByIds_SingleId() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1"));

        CapacityWithTechnologies capacity = new CapacityWithTechnologies();
        capacity.setId(1L);

        when(capacityRepository.findCapacitiesByIdsWithTechnologies(any(List.class)))
            .thenReturn(Flux.just(capacity));
        when(capacityListMapper.toSimpleWithTechnologiesResponse(any()))
            .thenReturn(mock(CapacityWithTechnologiesSimpleResponse.class));

        // Act & Assert
        StepVerifier.create(capacityHandler.getCapacitiesByIds(serverRequest))
            .expectNextMatches(serverResponse -> serverResponse.statusCode().value() == 200)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle invalid IDs format in getCapacitiesByIds")
    void testGetCapacitiesByIds_InvalidFormat() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1,invalid,3"));

        // Act & Assert
        StepVerifier.create(capacityHandler.getCapacitiesByIds(serverRequest))
            .expectError(NumberFormatException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle repository error in getCapacitiesByIds")
    void testGetCapacitiesByIds_RepositoryError() {
        // Arrange
        when(serverRequest.queryParam("ids")).thenReturn(Optional.of("1,2,3"));

        when(capacityRepository.findCapacitiesByIdsWithTechnologies(any(List.class)))
            .thenReturn(Flux.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(capacityHandler.getCapacitiesByIds(serverRequest))
            .expectError(RuntimeException.class)
            .verify();
    }

    // Helper methods
    private ListCapacitiesRequest createListCapacitiesRequest(int page, int size, String sortBy, String sortOrder) {
        return ListCapacitiesRequest.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortOrder(sortOrder)
            .build();
    }

    private co.com.bancolombia.model.common.PageRequest createPageRequest(
            int page, int size, String sortBy, SortDirection sortDirection) {
        return co.com.bancolombia.model.common.PageRequest.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();
    }

    private Page<CapacityWithTechnologies> createEmptyPage(int currentPage) {
        return Page.<CapacityWithTechnologies>builder()
            .content(new ArrayList<>())
            .currentPage(currentPage)
            .totalPages(0)
            .totalElements(0)
            .pageSize(10)
            .hasNextPage(false)
            .hasPreviousPage(currentPage > 0)
            .sortBy("name")
            .sortDirection(SortDirection.ASC)
            .build();
    }
}
