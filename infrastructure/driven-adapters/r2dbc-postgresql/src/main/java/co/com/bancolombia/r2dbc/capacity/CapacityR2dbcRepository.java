package co.com.bancolombia.r2dbc.capacity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityR2dbcRepository extends ReactiveCrudRepository<CapacityData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM capacities WHERE name = :name)")
    Mono<Boolean> existsByName(@Param("name") String name);

    @Query("SELECT COUNT(*) FROM capacities")
    Mono<Long> countAll();

    @Query("SELECT c.id, c.name, c.description, c.created_at, c.updated_at FROM capacities c ORDER BY c.name ASC LIMIT :limit OFFSET :offset")
    Flux<CapacityData> findAllOrderByNameAsc(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT c.id, c.name, c.description, c.created_at, c.updated_at FROM capacities c ORDER BY c.name DESC LIMIT :limit OFFSET :offset")
    Flux<CapacityData> findAllOrderByNameDesc(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT c.id, c.name, c.description, c.created_at, c.updated_at FROM capacities c LEFT JOIN capacity_technologies ct ON c.id = ct.capacity_id GROUP BY c.id, c.name, c.description, c.created_at, c.updated_at ORDER BY COUNT(ct.technology_id) ASC, c.name ASC LIMIT :limit OFFSET :offset")
    Flux<CapacityData> findAllOrderByTechnologyCountAsc(@Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT c.id, c.name, c.description, c.created_at, c.updated_at FROM capacities c LEFT JOIN capacity_technologies ct ON c.id = ct.capacity_id GROUP BY c.id, c.name, c.description, c.created_at, c.updated_at ORDER BY COUNT(ct.technology_id) DESC, c.name ASC LIMIT :limit OFFSET :offset")
    Flux<CapacityData> findAllOrderByTechnologyCountDesc(@Param("limit") int limit, @Param("offset") long offset);
}
