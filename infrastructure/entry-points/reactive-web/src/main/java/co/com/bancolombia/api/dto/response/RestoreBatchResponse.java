package co.com.bancolombia.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RestoreBatchResponse {

    @JsonProperty("restored_count")
    private Integer restoredCount;

    @JsonProperty("capacities_restored")
    private List<Long> capacitiesRestored;
}
