package co.com.bancolombia.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainErrorCode {
    CAPACITY_NAME_REQUIRED("CAPACITY_NAME_REQUIRED", "El nombre de la capacidad es obligatorio"),
    CAPACITY_DESCRIPTION_REQUIRED("CAPACITY_DESCRIPTION_REQUIRED", "La descripción de la capacidad es obligatoria"),
    CAPACITY_NAME_ALREADY_EXISTS("CAPACITY_NAME_ALREADY_EXISTS", "Ya existe una capacidad con este nombre"),
    INVALID_CAPACITY_NAME_LENGTH("INVALID_CAPACITY_NAME_LENGTH", "El nombre de la capacidad no debe exceder 50 caracteres"),
    INVALID_CAPACITY_DESCRIPTION_LENGTH("INVALID_CAPACITY_DESCRIPTION_LENGTH", "La descripción de la capacidad no debe exceder 90 caracteres"),
    MIN_TECHNOLOGIES_REQUIRED("MIN_TECHNOLOGIES_REQUIRED", "La capacidad debe tener mínimo 3 tecnologías asociadas"),
    MAX_TECHNOLOGIES_EXCEEDED("MAX_TECHNOLOGIES_EXCEEDED", "La capacidad no puede tener más de 20 tecnologías"),
    DUPLICATE_TECHNOLOGIES("DUPLICATE_TECHNOLOGIES", "La capacidad no puede tener tecnologías duplicadas"),
    TECHNOLOGY_IDS_REQUIRED("TECHNOLOGY_IDS_REQUIRED", "Debe proporcionar al menos una tecnología"),
    TECHNOLOGIES_NOT_FOUND("TECHNOLOGIES_NOT_FOUND", "Una o más tecnologías no existen"),
    TECHNOLOGY_VALIDATION_FAILED("TECHNOLOGY_VALIDATION_FAILED", "Error al validar las tecnologías"),
    TECHNOLOGY_SERVICE_UNAVAILABLE("TECHNOLOGY_SERVICE_UNAVAILABLE", "El servicio de tecnologías no está disponible"),
    INVALID_PAGE_NUMBER("INVALID_PAGE_NUMBER", "El número de página debe ser mayor o igual a 0"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE", "El tamaño de página debe estar entre 1 y 50"),
    INVALID_SORT_FIELD("INVALID_SORT_FIELD", "Campo de ordenamiento no válido");

    private final String code;
    private final String message;
}
