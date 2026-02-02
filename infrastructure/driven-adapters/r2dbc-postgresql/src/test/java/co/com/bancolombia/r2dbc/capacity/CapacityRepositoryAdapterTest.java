package co.com.bancolombia.r2dbc.capacity;

import co.com.bancolombia.model.capacity.Capacity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CapacityRepositoryAdapter Tests")
class CapacityRepositoryAdapterTest {

    @Mock
    private CapacityR2dbcRepository capacityRepository;

    @Mock
    private CapacityTechnologyR2dbcRepository capacityTechnologyRepository;

    @Mock
    private ObjectMapper mapper;

    private CapacityRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CapacityRepositoryAdapter(capacityRepository, capacityTechnologyRepository, mapper);
    }

    @Test
    @DisplayName("Should check if capacity name exists")
    void testExistsByName_True() {
        // Arrange
        when(capacityRepository.existsByName("Backend"))
            .thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(adapter.existsByName("Backend"))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when capacity name does not exist")
    void testExistsByName_False() {
        // Arrange
        when(capacityRepository.existsByName("NonExistent"))
            .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(adapter.existsByName("NonExistent"))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    @DisplayName("Should find capacity by id with technologies")
    void testFindById() {
        // Arrange
        Long capacityId = 1L;
        CapacityData capacityData = CapacityData.builder()
            .id(capacityId)
            .name("Backend")
            .description("Backend capabilities")
            .build();

        when(capacityRepository.findById(capacityId))
            .thenReturn(Mono.just(capacityData));
        when(capacityTechnologyRepository.findTechnologyIdsByCapacityId(capacityId))
            .thenReturn(Flux.fromIterable(Arrays.asList(1L, 2L, 3L)));
        when(mapper.map(capacityData, Capacity.class))
            .thenReturn(Capacity.builder()
                .id(capacityId)
                .name("Backend")
                .description("Backend capabilities")
                .build());

        // Act & Assert
        StepVerifier.create(adapter.findById(capacityId))
            .expectNextMatches(c -> c.getId().equals(capacityId) && c.getTechnologyIds().size() == 3)
            .verifyComplete();
    }
}
