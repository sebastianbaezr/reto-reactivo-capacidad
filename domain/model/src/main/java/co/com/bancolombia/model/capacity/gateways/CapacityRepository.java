package co.com.bancolombia.model.capacity.gateways;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacityRepository {
    Mono<Capacity> save(Capacity capacity);
    Mono<Boolean> existsByName(String name);
    Mono<Capacity> findById(Long id);
    Flux<Capacity> findAll();
    Mono<Page<CapacityWithTechnologies>> findAllWithPagination(PageRequest pageRequest);
    Mono<Long> count();
    Flux<Long> findExistingIds(List<Long> ids);
    Flux<Capacity> findCapacitiesByIds(List<Long> ids);
    Flux<CapacityWithTechnologies> findCapacitiesByIdsWithTechnologies(List<Long> ids);
    Mono<Long> softDeleteCapacity(Long capacityId);
    Mono<Long> restoreCapacity(Long capacityId);
    Mono<Long> countCapacitiesByTechnologyId(Long technologyId);
}
