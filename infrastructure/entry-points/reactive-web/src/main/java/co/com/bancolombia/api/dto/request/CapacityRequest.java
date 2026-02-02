package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CapacityRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no debe exceder 50 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 90, message = "La descripción no debe exceder 90 caracteres")
    private String description;

    @NotNull(message = "Debe proporcionar tecnologías")
    @Size(min = 3, max = 20, message = "Debe proporcionar entre 3 y 20 tecnologías")
    private List<Long> technologyIds;
}
