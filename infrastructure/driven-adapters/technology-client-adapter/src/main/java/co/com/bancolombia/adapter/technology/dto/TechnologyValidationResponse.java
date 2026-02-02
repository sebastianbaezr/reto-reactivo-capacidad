package co.com.bancolombia.adapter.technology.dto;

import java.util.List;

public record TechnologyValidationResponse(
    Boolean allExist,
    List<Long> existingIds,
    List<Long> notFoundIds
) {
    public Boolean isValid() {
        return allExist != null && allExist;
    }
}
