package co.com.bancolombia.r2dbc.capacity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("capacity_technologies")
public class CapacityTechnologyData {
    @Id
    private Long id;
    private Long capacityId;
    private Long technologyId;
}
