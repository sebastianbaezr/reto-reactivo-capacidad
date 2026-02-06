package co.com.bancolombia.r2dbc.capacity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacityTechnologyR2dbcRepository extends ReactiveCrudRepository<CapacityTechnologyData, Long> {

    @Query("SELECT technology_id FROM capacity_technologies WHERE capacity_id = :capacityId")
    Flux<Long> findTechnologyIdsByCapacityId(@Param("capacityId") Long capacityId);

    @Query("DELETE FROM capacity_technologies WHERE capacity_id = :capacityId")
    Mono<Void> deleteByCapacityId(@Param("capacityId") Long capacityId);

    @Query("SELECT COUNT(DISTINCT capacity_id) FROM capacity_technologies WHERE technology_id = :technologyId")
    Mono<Long> countCapacitiesByTechnologyId(@Param("technologyId") Long technologyId);

    @Query("SELECT capacity_id FROM capacity_technologies WHERE technology_id = :technologyId")
    Flux<Long> findCapacityIdsByTechnologyId(@Param("technologyId") Long technologyId);

    @Query("SELECT DISTINCT technology_id FROM capacity_technologies WHERE capacity_id IN (:capacityIds)")
    Flux<Long> findTechnologyIdsByCapacityIds(@Param("capacityIds") List<Long> capacityIds);

    @Query("SELECT COUNT(DISTINCT technology_id) FROM capacity_technologies WHERE capacity_id = :capacityId")
    Mono<Long> countTechnologiesByCapacityId(@Param("capacityId") Long capacityId);
}
