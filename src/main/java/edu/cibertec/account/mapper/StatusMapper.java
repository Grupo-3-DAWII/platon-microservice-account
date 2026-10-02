package edu.cibertec.account.mapper;

import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.StatusRequest;
import edu.cibertec.account.dto.StatusResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StatusMapper {

    Status toEntity(StatusRequest request);

    void updateEntity(@MappingTarget Status status, StatusRequest request);

    StatusResponse toResponse(Status status);
}
