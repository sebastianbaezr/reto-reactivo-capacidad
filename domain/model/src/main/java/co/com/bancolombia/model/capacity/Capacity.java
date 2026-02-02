package co.com.bancolombia.model.capacity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import co.com.bancolombia.model.common.AuditableModel;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Capacity extends AuditableModel {
    private Long id;
    private String name;
    private String description;
    private List<Long> technologyIds;
}
