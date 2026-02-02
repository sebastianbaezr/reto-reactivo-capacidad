package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ListCapacitiesRequest {

    @Default
    @Min(value = 0, message = "El número de página debe ser mayor o igual a 0")
    private Integer page = 0;

    @Default
    @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
    @Max(value = 50, message = "El tamaño de página no puede exceder 50")
    private Integer size = 10;

    @Default
    private String sortBy = "name";

    @Default
    private String sortOrder = "asc";
}
