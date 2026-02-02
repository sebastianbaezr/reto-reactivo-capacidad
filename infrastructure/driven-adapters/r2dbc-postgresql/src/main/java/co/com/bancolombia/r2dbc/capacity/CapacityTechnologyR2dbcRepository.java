package co.com.bancolombia.r2dbc.capacity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityTechnologyR2dbcRepository extends ReactiveCrudRepository<CapacityTechnologyData, Long> {

    @Query("SELECT technology_id FROM capacity_technologies WHERE capacity_id = :capacityId")
    Flux<Long> findTechnologyIdsByCapacityId(@Param("capacityId") Long capacityId);

    @Query("DELETE FROM capacity_technologies WHERE capacity_id = :capacityId")
    Mono<Void> deleteByCapacityId(@Param("capacityId") Long capacityId);
}
