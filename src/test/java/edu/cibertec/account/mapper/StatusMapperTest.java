package edu.cibertec.account.mapper;

import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.StatusRequest;
import edu.cibertec.account.dto.StatusResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class StatusMapperTest {

    private final StatusMapper mapper = Mappers.getMapper(StatusMapper.class);

    @Test
    void shouldMapStatusToResponse() {
        StatusResponse response = mapper.toResponse(Status.builder()
                .idStatus(1L).name("active").build());

        assertThat(response).isEqualTo(new StatusResponse(1L, "active"));
    }

    @Test
    void shouldMapRequestToEntity() {
        Status status = mapper.toEntity(new StatusRequest("active"));

        assertThat(status.getName()).isEqualTo("active");
    }

    @Test
    void shouldUpdateEntityFromRequest() {
        Status status = Status.builder().idStatus(1L).name("active").build();

        mapper.updateEntity(status, new StatusRequest("inactive"));

        assertThat(status.getIdStatus()).isEqualTo(1L);
        assertThat(status.getName()).isEqualTo("inactive");
    }
}
