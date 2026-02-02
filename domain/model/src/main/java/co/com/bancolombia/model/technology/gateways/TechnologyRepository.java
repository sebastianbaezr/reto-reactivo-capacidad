package co.com.bancolombia.model.technology.gateways;

import co.com.bancolombia.model.technology.TechnologySummary;
import reactor.core.publisher.Flux;

import java.util.List;

public interface TechnologyRepository {
    Flux<TechnologySummary> findByIds(List<Long> ids);
}
