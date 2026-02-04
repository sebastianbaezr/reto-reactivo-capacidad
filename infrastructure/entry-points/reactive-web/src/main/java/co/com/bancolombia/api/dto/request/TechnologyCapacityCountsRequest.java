package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TechnologyCapacityCountsRequest {

    @NotNull(message = "Technology IDs are required")
    private List<Long> technologyIds;
}
