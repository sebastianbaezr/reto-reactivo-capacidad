package co.com.bancolombia.usecase.listcapacities;

import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListCapacitiesUseCase {
    private final CapacityRepository capacityRepository;

    private static final int MAX_PAGE_SIZE = 50;
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_TECHNOLOGY_COUNT = "technologyCount";

    public Mono<Page<CapacityWithTechnologies>> execute(PageRequest pageRequest) {
        return Mono.defer(() -> validatePageNumber(pageRequest))
            .flatMap(this::validatePageSize)
            .flatMap(this::validateSortField)
            .flatMap(capacityRepository::findAllWithPagination);
    }

    private Mono<PageRequest> validatePageNumber(PageRequest pageRequest) {
        return pageRequest.getPage() < 0
            ? Mono.error(new BusinessException(DomainErrorCode.INVALID_PAGE_NUMBER))
            : Mono.just(pageRequest);
    }

    private Mono<PageRequest> validatePageSize(PageRequest pageRequest) {
        return (pageRequest.getSize() < 1 || pageRequest.getSize() > MAX_PAGE_SIZE)
            ? Mono.error(new BusinessException(DomainErrorCode.INVALID_PAGE_SIZE))
            : Mono.just(pageRequest);
    }

    private Mono<PageRequest> validateSortField(PageRequest pageRequest) {
        String sortBy = pageRequest.getSortBy();
        return (sortBy != null && !sortBy.equals(SORT_BY_NAME) && !sortBy.equals(SORT_BY_TECHNOLOGY_COUNT))
            ? Mono.error(new BusinessException(DomainErrorCode.INVALID_SORT_FIELD))
            : Mono.just(pageRequest);
    }
}
