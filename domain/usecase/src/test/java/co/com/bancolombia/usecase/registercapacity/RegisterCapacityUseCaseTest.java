package co.com.bancolombia.usecase.registercapacity;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.gateways.TechnologyValidationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCapacityUseCase Tests")
class RegisterCapacityUseCaseTest {

    @Mock
    private CapacityRepository capacityRepository;

    @Mock
    private TechnologyValidationGateway technologyValidationGateway;

    private RegisterCapacityUseCase registerCapacityUseCase;

    @BeforeEach
    void setUp() {
        registerCapacityUseCase = new RegisterCapacityUseCase(
            capacityRepository,
            technologyValidationGateway
        );
    }

    @Test
    @DisplayName("Should save capacity successfully when all validations pass")
    void testExecute_Success() {
        // Arrange
        Capacity capacity = createCapacity("Backend", "Backend capabilities", Arrays.asList(1L, 2L, 3L));
        Capacity savedCapacity = createCapacity("Backend", "Backend capabilities", Arrays.asList(1L, 2L, 3L));
        savedCapacity.setId(1L);

        when(capacityRepository.existsByName("Backend"))
            .thenReturn(Mono.just(false));
        when(technologyValidationGateway.validateTechnologiesExist(Arrays.asList(1L, 2L, 3L)))
            .thenReturn(Mono.just(true));
        when(capacityRepository.save(any(Capacity.class)))
            .thenReturn(Mono.just(savedCapacity));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectNextMatches(c -> c.getId() != null && c.getId().equals(1L))
            .verifyComplete();

        verify(capacityRepository).existsByName("Backend");
        verify(technologyValidationGateway).validateTechnologiesExist(Arrays.asList(1L, 2L, 3L));
        verify(capacityRepository).save(any(Capacity.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when name already exists")
    void testExecute_DuplicateName() {
        // Arrange
        Capacity capacity = createCapacity("Frontend", "Frontend capabilities", Arrays.asList(4L, 5L, 6L));

        when(capacityRepository.existsByName("Frontend"))
            .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.CAPACITY_NAME_ALREADY_EXISTS.getCode()))
            .verify();

        verify(capacityRepository).existsByName("Frontend");
    }

    @Test
    @DisplayName("Should throw BusinessException when less than 3 technologies")
    void testExecute_InsufficientTechnologies() {
        // Arrange
        Capacity capacity = createCapacity("DevOps", "DevOps capabilities", Arrays.asList(1L, 2L));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.MIN_TECHNOLOGIES_REQUIRED.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when duplicate technologies")
    void testExecute_DuplicateTechnologies() {
        // Arrange
        Capacity capacity = createCapacity("Mobile", "Mobile capabilities", Arrays.asList(1L, 2L, 3L, 1L));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.DUPLICATE_TECHNOLOGIES.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when name is null")
    void testExecute_NullName() {
        // Arrange
        Capacity capacity = new Capacity();
        capacity.setName(null);
        capacity.setDescription("Some description");
        capacity.setTechnologyIds(Arrays.asList(1L, 2L, 3L));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.CAPACITY_NAME_REQUIRED.getCode()))
            .verify();
    }

    @Test
    @DisplayName("Should throw BusinessException when technologies do not exist")
    void testExecute_TechnologiesNotFound() {
        // Arrange
        Capacity capacity = createCapacity("DataEngineering", "Data engineering capabilities", Arrays.asList(1L, 2L, 3L));

        when(capacityRepository.existsByName("DataEngineering"))
            .thenReturn(Mono.just(false));
        when(technologyValidationGateway.validateTechnologiesExist(Arrays.asList(1L, 2L, 3L)))
            .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.TECHNOLOGIES_NOT_FOUND.getCode()))
            .verify();

        verify(capacityRepository).existsByName("DataEngineering");
        verify(technologyValidationGateway).validateTechnologiesExist(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    @DisplayName("Should throw BusinessException when technology service is unavailable")
    void testExecute_TechnologyServiceUnavailable() {
        // Arrange
        Capacity capacity = createCapacity("QA", "QA capabilities", Arrays.asList(1L, 2L, 3L));

        when(capacityRepository.existsByName("QA"))
            .thenReturn(Mono.just(false));
        when(technologyValidationGateway.validateTechnologiesExist(Arrays.asList(1L, 2L, 3L)))
            .thenReturn(Mono.error(new BusinessException(DomainErrorCode.TECHNOLOGY_SERVICE_UNAVAILABLE)));

        // Act & Assert
        StepVerifier.create(registerCapacityUseCase.execute(capacity))
            .expectErrorMatches(error -> error instanceof BusinessException &&
                ((BusinessException) error).getCode().equals(DomainErrorCode.TECHNOLOGY_SERVICE_UNAVAILABLE.getCode()))
            .verify();

        verify(capacityRepository).existsByName("QA");
        verify(technologyValidationGateway).validateTechnologiesExist(Arrays.asList(1L, 2L, 3L));
    }

    // Helper method
    private Capacity createCapacity(String name, String description, List<Long> technologyIds) {
        Capacity capacity = new Capacity();
        capacity.setName(name);
        capacity.setDescription(description);
        capacity.setTechnologyIds(technologyIds);
        return capacity;
    }
}
