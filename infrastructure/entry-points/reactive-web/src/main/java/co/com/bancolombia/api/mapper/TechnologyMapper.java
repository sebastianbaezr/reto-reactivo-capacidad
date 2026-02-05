package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.response.TechnologyCapacityCountResponse;
import co.com.bancolombia.api.dto.response.TechnologyCapacityCountsResponse;
import co.com.bancolombia.model.technology.TechnologyCapacityCountWithRelated;
import org.mapstruct.Mapper;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TechnologyMapper {

    default TechnologyCapacityCountResponse toTechnologyCapacityCountResponse(Long technologyId, TechnologyCapacityCountWithRelated countWithRelated) {
        return TechnologyCapacityCountResponse.builder()
            .technologyId(technologyId)
            .capacityCount(countWithRelated.getCapacityCount())
            .relatedTechnologyIds(countWithRelated.getRelatedTechnologyIds())
            .build();
    }

    default TechnologyCapacityCountsResponse toTechnologyCapacityCountsResponse(Map<Long, Long> countsByTechnology) {
        Map<String, Long> stringKeyMap = new HashMap<>();
        countsByTechnology.forEach((key, value) -> stringKeyMap.put(String.valueOf(key), value));

        return TechnologyCapacityCountsResponse.builder()
            .technologyCounts(stringKeyMap)
            .build();
    }
}
