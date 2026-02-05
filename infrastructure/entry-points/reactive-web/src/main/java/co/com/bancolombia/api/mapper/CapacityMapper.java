package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.CapacityRequest;
import co.com.bancolombia.api.dto.response.CapacityResponse;
import co.com.bancolombia.api.dto.response.CapacitySimpleResponse;
import co.com.bancolombia.model.capacity.Capacity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapacityMapper {

    @Mapping(target = "id", ignore = true)
    Capacity toDomain(CapacityRequest request);

    @Mapping(target = "technologies", ignore = true)
    CapacityResponse toResponse(Capacity entity);

    CapacitySimpleResponse toSimpleResponse(Capacity entity);
}
