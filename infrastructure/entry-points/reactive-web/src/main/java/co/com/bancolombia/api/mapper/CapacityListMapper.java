package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.ListCapacitiesRequest;
import co.com.bancolombia.api.dto.response.CapacityListItemResponse;
import co.com.bancolombia.api.dto.response.CapacityWithTechnologiesSimpleResponse;
import co.com.bancolombia.api.dto.response.PageResponse;
import co.com.bancolombia.api.dto.response.TechnologySummaryResponse;
import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.model.technology.TechnologySummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapacityListMapper {

    default PageRequest toPageRequest(ListCapacitiesRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "name";
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "asc";

        return PageRequest.builder()
            .page(request.getPage() != null ? request.getPage() : 0)
            .size(request.getSize() != null ? request.getSize() : 10)
            .sortBy(sortBy)
            .sortDirection("desc".equalsIgnoreCase(sortOrder)
                ? SortDirection.DESC
                : SortDirection.ASC)
            .build();
    }

    default PageResponse<CapacityListItemResponse> toPageResponse(
            Page<CapacityWithTechnologies> page) {

        List<CapacityListItemResponse> content = page.getContent().stream()
            .map(this::toListItemResponse)
            .toList();

        return PageResponse.<CapacityListItemResponse>builder()
            .content(content)
            .pageMetadata(PageResponse.PageMetadata.builder()
                .currentPage(page.getCurrentPage())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getPageSize())
                .hasNextPage(page.isHasNextPage())
                .hasPreviousPage(page.isHasPreviousPage())
                .build())
            .sortMetadata(PageResponse.SortMetadata.builder()
                .field(page.getSortBy())
                .direction(page.getSortDirection().name().toLowerCase())
                .build())
            .build();
    }

    @Mapping(target = "technologies", source = "technologies")
    CapacityListItemResponse toListItemResponse(CapacityWithTechnologies capacity);

    @Mapping(target = "technologies", source = "technologies")
    CapacityWithTechnologiesSimpleResponse toSimpleWithTechnologiesResponse(CapacityWithTechnologies capacity);

    TechnologySummaryResponse toTechnologySummaryResponse(TechnologySummary technology);

    List<TechnologySummaryResponse> toTechnologySummaryResponseList(
        List<TechnologySummary> technologies);
}
