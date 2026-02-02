package co.com.bancolombia.adapter.technology.dto;

import java.util.List;

public record TechnologyValidationResponse(
    TechnologyValidationData data
) {
    public record TechnologyValidationData(
        Boolean allExist,
        List<Long> existingIds,
        List<Long> notFoundIds
    ) {}

    public Boolean isValid() {
        return data != null && data.allExist;
    }
}
