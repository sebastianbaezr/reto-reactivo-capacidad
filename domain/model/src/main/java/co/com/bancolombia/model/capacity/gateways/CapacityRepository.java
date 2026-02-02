package co.com.bancolombia.model.capacity.gateways;

import co.com.bancolombia.model.capacity.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityRepository {
    Mono<Capacity> save(Capacity capacity);
    Mono<Boolean> existsByName(String name);
    Mono<Capacity> findById(Long id);
    Flux<Capacity> findAll();
}
