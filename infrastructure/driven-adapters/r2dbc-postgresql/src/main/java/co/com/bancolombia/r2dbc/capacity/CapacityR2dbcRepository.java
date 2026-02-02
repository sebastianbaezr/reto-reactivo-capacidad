package co.com.bancolombia.r2dbc.capacity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CapacityR2dbcRepository extends ReactiveCrudRepository<CapacityData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM capacities WHERE name = :name)")
    Mono<Boolean> existsByName(@Param("name") String name);
}
