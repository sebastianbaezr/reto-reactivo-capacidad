package co.com.bancolombia.model.technology.gateways;

import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyValidationGateway {
    Mono<Boolean> validateTechnologiesExist(List<Long> technologyIds);
}
