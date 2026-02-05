package co.com.bancolombia.api;

import co.com.bancolombia.api.handler.CapacityHandler;
import co.com.bancolombia.api.handler.TechnologyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(
            Handler handler,
            Optional<CapacityHandler> capacityHandler,
            Optional<TechnologyHandler> technologyHandler) {
        var router = route(GET("/api/usecase/path"), handler::listenGETUseCase)
            .andRoute(GET("/api/otherusercase/path"), handler::listenGETOtherUseCase)
            .andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase);

        if (capacityHandler.isPresent()) {
            router = router
                .andRoute(GET("/api/capacities"), capacityHandler.get()::listCapacities)
                .andRoute(GET("/api/capacities/validate"), capacityHandler.get()::validateCapacities)
                .andRoute(GET("/api/capacities/by-ids"), capacityHandler.get()::getCapacitiesByIds)
                .andRoute(GET("/api/capacities/{capacityId}/technologies"), capacityHandler.get()::getCapacityWithTechnologies)
                .andRoute(POST("/api/capacities"), capacityHandler.get()::registerCapacity)
                .andRoute(DELETE("/api/capacities/batch"), capacityHandler.get()::deleteCapacitiesBatch)
                .andRoute(POST("/api/capacities/restore-batch"), capacityHandler.get()::restoreCapacitiesBatch);
        }

        if (technologyHandler.isPresent()) {
            router = router
                .andRoute(GET("/api/technologies/{technologyId}/capacity-count"), technologyHandler.get()::getCapacityCount)
                .andRoute(POST("/api/technologies/capacity-counts"), technologyHandler.get()::getCapacityCounts);
        }

        return router;
    }
}
