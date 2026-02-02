package co.com.bancolombia.r2dbc.capacity;

import co.com.bancolombia.model.capacity.Capacity;
import co.com.bancolombia.model.capacity.gateways.CapacityRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
public class CapacityRepositoryAdapter extends ReactiveAdapterOperations<Capacity, CapacityData, Long, CapacityR2dbcRepository>
        implements CapacityRepository {

    private final CapacityTechnologyR2dbcRepository capacityTechnologyRepository;

    public CapacityRepositoryAdapter(
            CapacityR2dbcRepository repository,
            CapacityTechnologyR2dbcRepository capacityTechnologyRepository,
            ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Capacity.class));
        this.capacityTechnologyRepository = capacityTechnologyRepository;
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
}
