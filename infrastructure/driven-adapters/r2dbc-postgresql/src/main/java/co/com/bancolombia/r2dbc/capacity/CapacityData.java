package co.com.bancolombia.r2dbc.capacity;

import co.com.bancolombia.r2dbc.common.AuditableModelData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Table("capacities")
public class CapacityData extends AuditableModelData {
    @Id
    private Long id;
    private String name;
    private String description;
}
