package co.com.bancolombia.r2dbc.capacity;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import co.com.bancolombia.model.common.SortDirection;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.technology.TechnologySummary;
import co.com.bancolombia.model.technology.gateways.TechnologyRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class CapacityRepositoryAdapter extends ReactiveAdapterOperations<Capacity, CapacityData, Long, CapacityR2dbcRepository>
        implements CapacityRepository {

    private static final String SORT_BY_TECHNOLOGY_COUNT = "technologyCount";
    private static final String SORT_BY_NAME = "name";

    private final CapacityTechnologyR2dbcRepository capacityTechnologyRepository;
    private final TechnologyRepository technologyRepository;

    public CapacityRepositoryAdapter(
            CapacityR2dbcRepository repository,
            CapacityTechnologyR2dbcRepository capacityTechnologyRepository,
            TechnologyRepository technologyRepository,
            ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Capacity.class));
        this.capacityTechnologyRepository = capacityTechnologyRepository;
        this.technologyRepository = technologyRepository;
    }

    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return saveData(toData(capacity))
            .flatMap(savedData -> saveTechnologyAssociations(savedData.getId(), capacity.getTechnologyIds())
                .then(Mono.just(savedData)))
            .flatMap(this::loadCapacityWithTechnologies);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Mono<Capacity> findById(Long id) {
        return repository.findById(id)
            .flatMap(this::loadCapacityWithTechnologies);
    }

    @Override
    public Flux<Capacity> findAll() {
        return repository.findAll()
            .flatMap(this::loadCapacityWithTechnologies);
    }

    @Override
    public Mono<Long> count() {
        return repository.countAll();
    }

    @Override
    public Flux<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(ids)
            .flatMap(repository::findById)
            .map(CapacityData::getId);
    }

    @Override
    public Mono<Page<CapacityWithTechnologies>> findAllWithPagination(PageRequest pageRequest) {
        return count()
            .flatMap(totalElements -> getCapacitiesWithSorting(pageRequest)
                .collectList()
                .flatMap(capacityDataList -> processCapacitiesList(capacityDataList, pageRequest, totalElements)));
    }

    private Mono<Page<CapacityWithTechnologies>> processCapacitiesList(
            List<CapacityData> capacityDataList,
            PageRequest pageRequest,
            long totalElements) {
        return capacityDataList.isEmpty()
            ? Mono.just(buildEmptyPage(pageRequest, totalElements))
            : loadAndMapTechnologies(capacityDataList, pageRequest, totalElements);
    }

    private Mono<Page<CapacityWithTechnologies>> loadAndMapTechnologies(
            List<CapacityData> capacityDataList,
            PageRequest pageRequest,
            long totalElements) {
        List<Long> capacityIds = capacityDataList.stream()
            .map(CapacityData::getId)
            .toList();

        return loadAllTechnologiesForCapacities(capacityIds)
            .collectList()
            .flatMap(techMappings -> enrichCapacitiesWithTechnologies(
                capacityDataList, techMappings, pageRequest, totalElements));
    }

    private Mono<Page<CapacityWithTechnologies>> enrichCapacitiesWithTechnologies(
            List<CapacityData> capacityDataList,
            List<CapacityTechnologyMapping> techMappings,
            PageRequest pageRequest,
            long totalElements) {
        List<Long> uniqueTechIds = techMappings.stream()
            .map(CapacityTechnologyMapping::getTechnologyId)
            .distinct()
            .toList();

        return technologyRepository.findByIds(uniqueTechIds)
            .collectList()
            .map(technologies -> buildCapacityPage(capacityDataList, techMappings, technologies, pageRequest, totalElements));
    }

    private Page<CapacityWithTechnologies> buildCapacityPage(
            List<CapacityData> capacityDataList,
            List<CapacityTechnologyMapping> techMappings,
            List<TechnologySummary> technologies,
            PageRequest pageRequest,
            long totalElements) {
        Map<Long, TechnologySummary> techMap = technologies.stream()
            .collect(Collectors.toMap(TechnologySummary::getId, tech -> tech));

        List<CapacityWithTechnologies> capacities = buildCapacitiesWithTechnologies(
            capacityDataList, techMappings, techMap);

        return buildPage(capacities, pageRequest, totalElements);
    }

    private Flux<CapacityData> getCapacitiesWithSorting(PageRequest pageRequest) {
        int limit = pageRequest.getSize();
        long offset = pageRequest.getOffset();
        String sortBy = pageRequest.getSortBy();
        SortDirection direction = pageRequest.getSortDirection();

        if (SORT_BY_TECHNOLOGY_COUNT.equals(sortBy)) {
            return direction == SortDirection.ASC
                ? repository.findAllOrderByTechnologyCountAsc(limit, offset)
                : repository.findAllOrderByTechnologyCountDesc(limit, offset);
        } else {
            return direction == SortDirection.ASC
                ? repository.findAllOrderByNameAsc(limit, offset)
                : repository.findAllOrderByNameDesc(limit, offset);
        }
    }

    private Flux<CapacityTechnologyMapping> loadAllTechnologiesForCapacities(List<Long> capacityIds) {
        return Flux.fromIterable(capacityIds)
            .flatMap(capacityId ->
                capacityTechnologyRepository.findTechnologyIdsByCapacityId(capacityId)
                    .map(techId -> new CapacityTechnologyMapping(capacityId, techId))
            );
    }

    private List<CapacityWithTechnologies> buildCapacitiesWithTechnologies(
            List<CapacityData> capacityDataList,
            List<CapacityTechnologyMapping> techMappings,
            Map<Long, TechnologySummary> techMap) {

        Map<Long, List<TechnologySummary>> techsByCapacity = techMappings.stream()
            .collect(Collectors.groupingBy(
                CapacityTechnologyMapping::getCapacityId,
                Collectors.mapping(
                    mapping -> techMap.get(mapping.getTechnologyId()),
                    Collectors.toList()
                )
            ));

        return capacityDataList.stream()
            .map(data -> {
                List<TechnologySummary> technologies =
                    techsByCapacity.getOrDefault(data.getId(), List.of());

                CapacityWithTechnologies capacity = CapacityWithTechnologies.builder()
                    .id(data.getId())
                    .name(data.getName())
                    .description(data.getDescription())
                    .technologies(technologies)
                    .build();
                capacity.setCreatedAt(data.getCreatedAt());
                capacity.setUpdatedAt(data.getUpdatedAt());
                return capacity;
            })
            .toList();
    }

    private Page<CapacityWithTechnologies> buildPage(
            List<CapacityWithTechnologies> content,
            PageRequest pageRequest,
            long totalElements) {

        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());

        return Page.<CapacityWithTechnologies>builder()
            .content(content)
            .currentPage(pageRequest.getPage())
            .totalPages(totalPages)
            .totalElements(totalElements)
            .pageSize(pageRequest.getSize())
            .sortBy(pageRequest.getSortBy())
            .sortDirection(pageRequest.getSortDirection())
            .hasNextPage(pageRequest.getPage() + 1 < totalPages)
            .hasPreviousPage(pageRequest.getPage() > 0)
            .build();
    }

    private Page<CapacityWithTechnologies> buildEmptyPage(
            PageRequest pageRequest,
            long totalElements) {

        return Page.<CapacityWithTechnologies>builder()
            .content(List.of())
            .currentPage(pageRequest.getPage())
            .totalPages(0)
            .totalElements(totalElements)
            .pageSize(pageRequest.getSize())
            .sortBy(pageRequest.getSortBy())
            .sortDirection(pageRequest.getSortDirection())
            .hasNextPage(false)
            .hasPreviousPage(false)
            .build();
    }

    private Mono<Void> saveTechnologyAssociations(Long capacityId, List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            return Mono.empty();
        }

        Flux<CapacityTechnologyData> associations = Flux.fromIterable(technologyIds)
            .map(techId -> CapacityTechnologyData.builder()
                .capacityId(capacityId)
                .technologyId(techId)
                .build());

        return capacityTechnologyRepository.saveAll(associations).then();
    }

    private Mono<Capacity> loadCapacityWithTechnologies(CapacityData capacityData) {
        return capacityTechnologyRepository.findTechnologyIdsByCapacityId(capacityData.getId())
            .collectList()
            .map(techIds -> {
                Capacity capacity = toEntity(capacityData);
                capacity.setTechnologyIds(techIds);
                return capacity;
            });
    }

    @Override
    public Flux<Capacity> findCapacitiesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(ids)
            .flatMap(repository::findById)
            .flatMap(this::loadCapacityWithTechnologies);
    }

    @Override
    public Flux<CapacityWithTechnologies> findCapacitiesByIdsWithTechnologies(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(ids)
            .flatMap(repository::findById)
            .collectList()
            .flatMap(capacityDataList -> loadAndEnrichCapacitiesWithTechnologies(capacityDataList))
            .flatMapMany(Flux::fromIterable);
    }

    private Mono<List<CapacityWithTechnologies>> loadAndEnrichCapacitiesWithTechnologies(
            List<CapacityData> capacityDataList) {
        List<Long> capacityIds = capacityDataList.stream()
            .map(CapacityData::getId)
            .toList();

        return loadAllTechnologiesForCapacities(capacityIds)
            .collectList()
            .flatMap(techMappings -> enrichCapacitiesWithTechnologiesData(capacityDataList, techMappings));
    }

    private Mono<List<CapacityWithTechnologies>> enrichCapacitiesWithTechnologiesData(
            List<CapacityData> capacityDataList,
            List<CapacityTechnologyMapping> techMappings) {
        List<Long> uniqueTechIds = techMappings.stream()
            .map(CapacityTechnologyMapping::getTechnologyId)
            .distinct()
            .toList();

        return technologyRepository.findByIds(uniqueTechIds)
            .collectList()
            .map(technologies -> {
                Map<Long, TechnologySummary> techMap = technologies.stream()
                    .collect(Collectors.toMap(TechnologySummary::getId, tech -> tech));
                return buildCapacitiesWithTechnologies(capacityDataList, techMappings, techMap);
            });
    }

    @Override
    public Mono<Long> softDeleteCapacity(Long capacityId) {
        return repository.findById(capacityId)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.CAPACITY_NOT_FOUND)))
            .flatMap(data -> repository.softDelete(capacityId)
                .then(Mono.just(capacityId)));
    }

    @Override
    public Mono<Long> restoreCapacity(Long capacityId) {
        return repository.findById(capacityId)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.CAPACITY_NOT_FOUND)))
            .flatMap(data -> repository.restore(capacityId)
                .then(Mono.just(capacityId)));
    }

    @Override
    public Mono<Long> countCapacitiesByTechnologyId(Long technologyId) {
        return capacityTechnologyRepository.countCapacitiesByTechnologyId(technologyId);
    }

    @AllArgsConstructor
    @Getter
    private static class CapacityTechnologyMapping {
        private final Long capacityId;
        private final Long technologyId;
    }
}
