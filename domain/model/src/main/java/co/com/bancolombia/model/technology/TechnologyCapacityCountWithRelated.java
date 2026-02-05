package co.com.bancolombia.model.technology;

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
public class TechnologyCapacityCountWithRelated {
    private Long capacityCount;
    private List<Long> relatedTechnologyIds;
}
