package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.ListCapacitiesRequest;
import co.com.bancolombia.api.dto.response.CapacityListItemResponse;
import co.com.bancolombia.api.dto.response.PageResponse;
import co.com.bancolombia.api.dto.response.TechnologySummaryResponse;
import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.model.technology.TechnologySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CapacityListMapper Tests")
class CapacityListMapperTest {

    private CapacityListMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CapacityListMapper.class);
    }

    // ===== toPageRequest Tests (6) =====

    @Test
    @DisplayName("Should map all fields from ListCapacitiesRequest to PageRequest")
    void testToPageRequest_AllFieldsProvided() {
        // Arrange
        ListCapacitiesRequest request = ListCapacitiesRequest.builder()
            .page(2)
            .size(25)
            .sortBy("technologyCount")
            .sortOrder("desc")
            .build();

        // Act
        PageRequest result = mapper.toPageRequest(request);

        // Assert
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(25);
        assertThat(result.getSortBy()).isEqualTo("technologyCount");
        assertThat(result.getSortDirection()).isEqualTo(SortDirection.DESC);
    }

    @Test
    @DisplayName("Should default page to 0 when null")
    void testToPageRequest_NullPage() {
        // Arrange
        ListCapacitiesRequest request = ListCapacitiesRequest.builder()
            .page(null)
            .size(10)
            .sortBy("name")
            .sortOrder("asc")
            .build();

        // Act
        PageRequest result = mapper.toPageRequest(request);

        // Assert
        assertThat(result.getPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should default size to 10 when null")
    void testToPageRequest_NullSize() {
        // Arrange
        ListCapacitiesRequest request = ListCapacitiesRequest.builder()
            .page(0)
            .size(null)
            .sortBy("name")
            .sortOrder("asc")
            .build();

        // Act
        PageRequest result = mapper.toPageRequest(request);

        // Assert
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should default sortBy to 'name' when null")
    void testToPageRequest_NullSortBy() {
        // Arrange
        ListCapacitiesRequest request = ListCapacitiesRequest.builder()
            .page(0)
            .size(10)
            .sortBy(null)
            .sortOrder("asc")
            .build();

        // Act
        PageRequest result = mapper.toPageRequest(request);

        // Assert
        assertThat(result.getSortBy()).isEqualTo("name");
    }

    @Test
    @DisplayName("Should default sortOrder to ASC when null")
    void testToPageRequest_NullSortOrder() {
        // Arrange
        ListCapacitiesRequest request = ListCapacitiesRequest.builder()
            .page(0)
            .size(10)
            .sortBy("name")
            .sortOrder(null)
            .build();

        // Act
        PageRequest result = mapper.toPageRequest(request);

        // Assert
        assertThat(result.getSortDirection()).isEqualTo(SortDirection.ASC);
    }

    @Test
    @DisplayName("Should map 'desc' to DESC and 'asc' to ASC (case-insensitive)")
    void testToPageRequest_SortOrderMapping() {
        // Arrange - Test DESC
        ListCapacitiesRequest descRequest = ListCapacitiesRequest.builder()
            .page(0)
            .size(10)
            .sortBy("name")
            .sortOrder("desc")
            .build();

        // Act
        PageRequest descResult = mapper.toPageRequest(descRequest);

        // Assert
        assertThat(descResult.getSortDirection()).isEqualTo(SortDirection.DESC);

        // Arrange - Test ASC
        ListCapacitiesRequest ascRequest = ListCapacitiesRequest.builder()
            .page(0)
            .size(10)
            .sortBy("name")
            .sortOrder("asc")
            .build();

        // Act
        PageRequest ascResult = mapper.toPageRequest(ascRequest);

        // Assert
        assertThat(ascResult.getSortDirection()).isEqualTo(SortDirection.ASC);
    }

    // ===== toPageResponse Tests (5) =====

    @Test
    @DisplayName("Should map Page to PageResponse with all content")
    void testToPageResponse_Success() {
        // Arrange
        CapacityWithTechnologies capacity = createCapacity(1L, "Backend");
        Page<CapacityWithTechnologies> page = Page.<CapacityWithTechnologies>builder()
            .content(Arrays.asList(capacity))
            .currentPage(0)
            .totalPages(3)
            .totalElements(30)
            .pageSize(10)
            .hasNextPage(true)
            .hasPreviousPage(false)
            .sortBy("name")
            .sortDirection(SortDirection.ASC)
            .build();

        // Act
        PageResponse<CapacityListItemResponse> result = mapper.toPageResponse(page);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageMetadata().getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageMetadata().getTotalPages()).isEqualTo(3);
        assertThat(result.getPageMetadata().getTotalElements()).isEqualTo(30);
        assertThat(result.getPageMetadata().getPageSize()).isEqualTo(10);
        assertThat(result.getPageMetadata().isHasNextPage()).isTrue();
        assertThat(result.getPageMetadata().isHasPreviousPage()).isFalse();
    }

    @Test
    @DisplayName("Should handle empty page content")
    void testToPageResponse_EmptyContent() {
        // Arrange
        Page<CapacityWithTechnologies> emptyPage = Page.<CapacityWithTechnologies>builder()
            .content(new ArrayList<>())
            .currentPage(0)
            .totalPages(0)
            .totalElements(0)
            .pageSize(10)
            .hasNextPage(false)
            .hasPreviousPage(false)
            .sortBy("name")
            .sortDirection(SortDirection.ASC)
            .build();

        // Act
        PageResponse<CapacityListItemResponse> result = mapper.toPageResponse(emptyPage);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPageMetadata().getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should map all page metadata fields correctly")
    void testToPageResponse_PageMetadata() {
        // Arrange
        CapacityWithTechnologies capacity = createCapacity(1L, "Backend");
        Page<CapacityWithTechnologies> page = Page.<CapacityWithTechnologies>builder()
            .content(Arrays.asList(capacity))
            .currentPage(1)
            .totalPages(5)
            .totalElements(50)
            .pageSize(10)
            .hasNextPage(true)
            .hasPreviousPage(true)
            .sortBy("technologyCount")
            .sortDirection(SortDirection.DESC)
            .build();

        // Act
        PageResponse<CapacityListItemResponse> result = mapper.toPageResponse(page);

        // Assert
        assertThat(result.getPageMetadata().getCurrentPage()).isEqualTo(1);
        assertThat(result.getPageMetadata().getTotalPages()).isEqualTo(5);
        assertThat(result.getPageMetadata().getTotalElements()).isEqualTo(50);
        assertThat(result.getPageMetadata().getPageSize()).isEqualTo(10);
        assertThat(result.getPageMetadata().isHasNextPage()).isTrue();
        assertThat(result.getPageMetadata().isHasPreviousPage()).isTrue();
    }

    @Test
    @DisplayName("Should map sort metadata with lowercase direction")
    void testToPageResponse_SortMetadata() {
        // Arrange
        CapacityWithTechnologies capacity = createCapacity(1L, "Backend");
        Page<CapacityWithTechnologies> page = Page.<CapacityWithTechnologies>builder()
            .content(Arrays.asList(capacity))
            .currentPage(0)
            .totalPages(1)
            .totalElements(1)
            .pageSize(10)
            .hasNextPage(false)
            .hasPreviousPage(false)
            .sortBy("name")
            .sortDirection(SortDirection.DESC)
            .build();

        // Act
        PageResponse<CapacityListItemResponse> result = mapper.toPageResponse(page);

        // Assert
        assertThat(result.getSortMetadata().getField()).isEqualTo("name");
        assertThat(result.getSortMetadata().getDirection()).isEqualTo("desc");
    }

    @Test
    @DisplayName("Should map sort direction to lowercase")
    void testToPageResponse_SortDirectionLowercase() {
        // Arrange
        CapacityWithTechnologies capacity = createCapacity(1L, "Backend");
        Page<CapacityWithTechnologies> pageAsc = Page.<CapacityWithTechnologies>builder()
            .content(Arrays.asList(capacity))
            .currentPage(0)
            .totalPages(1)
            .totalElements(1)
            .pageSize(10)
            .hasNextPage(false)
            .hasPreviousPage(false)
            .sortBy("name")
            .sortDirection(SortDirection.ASC)
            .build();

        // Act
        PageResponse<CapacityListItemResponse> resultAsc = mapper.toPageResponse(pageAsc);

        // Assert
        assertThat(resultAsc.getSortMetadata().getDirection()).isEqualTo("asc");

        // Test DESC
        Page<CapacityWithTechnologies> pageDesc = Page.<CapacityWithTechnologies>builder()
            .content(Arrays.asList(capacity))
            .currentPage(0)
            .totalPages(1)
            .totalElements(1)
            .pageSize(10)
            .hasNextPage(false)
            .hasPreviousPage(false)
            .sortBy("name")
            .sortDirection(SortDirection.DESC)
            .build();

        PageResponse<CapacityListItemResponse> resultDesc = mapper.toPageResponse(pageDesc);
        assertThat(resultDesc.getSortMetadata().getDirection()).isEqualTo("desc");
    }

    // ===== MapStruct Mapping Tests (4) =====

    @Test
    @DisplayName("Should map CapacityWithTechnologies to CapacityListItemResponse")
    void testToListItemResponse_Success() {
        // Arrange
        CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
            .id(1L)
            .name("Backend")
            .description("Backend capabilities")
            .build();

        // Act
        CapacityListItemResponse result = mapper.toListItemResponse(capacity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Backend");
    }

    @Test
    @DisplayName("Should map CapacityWithTechnologies to SimpleWithTechnologiesResponse")
    void testToSimpleWithTechnologiesResponse_Success() {
        // Arrange
        CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
            .id(2L)
            .name("DataScience")
            .description("Data science capabilities")
            .build();

        // Act
        var result = mapper.toSimpleWithTechnologiesResponse(capacity);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should map TechnologySummary to TechnologySummaryResponse")
    void testToTechnologySummaryResponse_Success() {
        // Arrange
        TechnologySummary technology = TechnologySummary.builder()
            .id(1L)
            .name("Java")
            .build();

        // Act
        TechnologySummaryResponse result = mapper.toTechnologySummaryResponse(technology);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should map list of TechnologySummary to TechnologySummaryResponse list")
    void testToTechnologySummaryResponseList_Success() {
        // Arrange
        TechnologySummary tech1 = TechnologySummary.builder()
            .id(1L)
            .name("Java")
            .build();

        TechnologySummary tech2 = TechnologySummary.builder()
            .id(2L)
            .name("Python")
            .build();

        List<TechnologySummary> technologies = Arrays.asList(tech1, tech2);

        // Act
        List<TechnologySummaryResponse> result = mapper.toTechnologySummaryResponseList(technologies);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    // Helper methods
    private CapacityWithTechnologies createCapacity(Long id, String name) {
        return CapacityWithTechnologies.builder()
            .id(id)
            .name(name)
            .description("Description for " + name)
            .build();
    }
}
