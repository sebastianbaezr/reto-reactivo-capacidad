package co.com.bancolombia.model.capacity.gateways;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.CapacityWithTechnologies;
import co.com.bancolombia.model.common.Page;
import co.com.bancolombia.model.common.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityRepository {
    Mono<Capacity> save(Capacity capacity);
    Mono<Boolean> existsByName(String name);
    Mono<Capacity> findById(Long id);
    Flux<Capacity> findAll();
    Mono<Page<CapacityWithTechnologies>> findAllWithPagination(PageRequest pageRequest);
    Mono<Long> count();
}
