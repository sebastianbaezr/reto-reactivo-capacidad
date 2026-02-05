package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.CapacityRequest;
import co.com.bancolombia.api.dto.response.CapacityResponse;
import co.com.bancolombia.api.dto.response.CapacitySimpleResponse;
import co.com.bancolombia.model.capacity.Capacity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CapacityMapper Tests")
class CapacityMapperTest {

    private CapacityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CapacityMapper.class);
    }

    @Test
    @DisplayName("Should map CapacityRequest to Capacity domain object")
    void testToDomain_Success() {
        // Arrange
        CapacityRequest request = CapacityRequest.builder()
            .name("Backend")
            .description("Backend capabilities")
            .technologyIds(Arrays.asList(1L, 2L, 3L))
            .build();

        // Act
        Capacity result = mapper.toDomain(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Backend");
        assertThat(result.getDescription()).isEqualTo("Backend capabilities");
        assertThat(result.getTechnologyIds()).hasSize(3);
        assertThat(result.getId()).isNull(); // @Mapping(target = "id", ignore = true)
    }

    @Test
    @DisplayName("Should ignore ID field when mapping request to entity")
    void testToDomain_IdIgnored() {
        // Arrange
        CapacityRequest request = CapacityRequest.builder()
            .name("Frontend")
            .description("Frontend capabilities")
            .technologyIds(Arrays.asList(4L, 5L, 6L))
            .build();

        // Act
        Capacity result = mapper.toDomain(request);

        // Assert
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("Should handle null description in request")
    void testToDomain_NullDescription() {
        // Arrange
        CapacityRequest request = CapacityRequest.builder()
            .name("DevOps")
            .description(null)
            .technologyIds(Arrays.asList(7L, 8L, 9L))
            .build();

        // Act
        Capacity result = mapper.toDomain(request);

        // Assert
        assertThat(result.getName()).isEqualTo("DevOps");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getTechnologyIds()).hasSize(3);
    }

    @Test
    @DisplayName("Should map Capacity to CapacityResponse")
    void testToResponse_Success() {
        // Arrange
        Capacity entity = Capacity.builder()
            .id(1L)
            .name("Backend")
            .description("Backend capabilities")
            .build();

        // Act
        CapacityResponse result = mapper.toResponse(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Backend");
        assertThat(result.getDescription()).isEqualTo("Backend capabilities");
        assertThat(result.getTechnologies()).isNull(); // @Mapping(target = "technologies", ignore = true)
    }

    @Test
    @DisplayName("Should ignore technologies field when mapping entity to response")
    void testToResponse_TechnologiesIgnored() {
        // Arrange
        Capacity entity = Capacity.builder()
            .id(2L)
            .name("Frontend")
            .description("Frontend capabilities")
            .build();

        // Act
        CapacityResponse result = mapper.toResponse(entity);

        // Assert
        assertThat(result.getTechnologies()).isNull();
    }

    @Test
    @DisplayName("Should handle null ID in entity when mapping to response")
    void testToResponse_NullId() {
        // Arrange
        Capacity entity = Capacity.builder()
            .id(null)
            .name("QA")
            .description("QA capabilities")
            .build();

        // Act
        CapacityResponse result = mapper.toResponse(entity);

        // Assert
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("QA");
    }

    @Test
    @DisplayName("Should map Capacity to CapacitySimpleResponse")
    void testToSimpleResponse_Success() {
        // Arrange
        Capacity entity = Capacity.builder()
            .id(3L)
            .name("DataEngineering")
            .description("Data engineering capabilities")
            .build();

        // Act
        CapacitySimpleResponse result = mapper.toSimpleResponse(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("DataEngineering");
    }

    @Test
    @DisplayName("Should populate all fields in simple response")
    void testToSimpleResponse_AllFieldsPopulated() {
        // Arrange
        Capacity entity = Capacity.builder()
            .id(4L)
            .name("Mobile")
            .description("Mobile development capabilities")
            .build();

        // Act
        CapacitySimpleResponse result = mapper.toSimpleResponse(entity);

        // Assert
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getName()).isEqualTo("Mobile");
    }

    @Test
    @DisplayName("Should handle null fields in simple response mapping")
    void testToSimpleResponse_NullFields() {
        // Arrange
        Capacity entity = Capacity.builder()
            .id(5L)
            .name("Testing")
            .description(null)
            .build();

        // Act
        CapacitySimpleResponse result = mapper.toSimpleResponse(entity);

        // Assert
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Testing");
    }
}
