package edu.cibertec.account.service;

import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.StatusRequest;
import edu.cibertec.account.dto.StatusResponse;
import edu.cibertec.account.exception.DuplicateResourceException;
import edu.cibertec.account.exception.ResourceNotFoundException;
import edu.cibertec.account.mapper.StatusMapper;
import edu.cibertec.account.repository.StatusRepository;
import edu.cibertec.account.service.impl.StatusServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private StatusRepository statusRepository;

    @Spy
    private StatusMapper statusMapper = Mappers.getMapper(StatusMapper.class);

    @InjectMocks
    private StatusServiceImpl statusService;

    @Test
    void shouldFindAllStatuses() {
        List<Status> statuses = List.of(Status.builder().idStatus(1L).name("active").build());
        when(statusRepository.findAll()).thenReturn(statuses);

        assertThat(statusService.findAll()).containsExactly(new StatusResponse(1L, "active"));
    }

    @Test
    void shouldFindStatusById() {
        Status status = Status.builder().idStatus(1L).name("active").build();
        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));

        assertThat(statusService.findById(1L)).isEqualTo(new StatusResponse(1L, "active"));
    }

    @Test
    void shouldRejectMissingStatus() {
        when(statusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Status not found");
    }

    @Test
    void shouldCreateStatusWhenNameIsUnique() {
        Status saved = Status.builder().idStatus(3L).name("pending").build();
        when(statusRepository.save(org.mockito.ArgumentMatchers.any(Status.class))).thenReturn(saved);

        assertThat(statusService.create(new StatusRequest("pending")))
                .isEqualTo(new StatusResponse(3L, "pending"));
    }

    @Test
    void shouldUpdateStatusWhenNameIsUnique() {
        Status status = Status.builder().idStatus(1L).name("active").build();
        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(statusRepository.save(status)).thenReturn(status);

        assertThat(statusService.update(1L, new StatusRequest("enabled")))
                .isEqualTo(new StatusResponse(1L, "enabled"));
    }

    @Test
    void shouldDeleteStatus() {
        Status status = Status.builder().idStatus(1L).name("active").build();
        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));

        statusService.delete(1L);

        org.mockito.Mockito.verify(statusRepository).delete(status);
    }

    @Test
    void shouldRejectDuplicateStatusNameOnCreate() {
        when(statusRepository.existsByName("active")).thenReturn(true);

        assertThatThrownBy(() -> statusService.create(new StatusRequest("active")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Status name already exists");
    }

    @Test
    void shouldRejectDuplicateStatusNameOnUpdate() {
        when(statusRepository.findById(1L)).thenReturn(Optional.of(Status.builder()
                .idStatus(1L).name("active").build()));
        when(statusRepository.existsByNameAndIdStatusNot("inactive", 1L)).thenReturn(true);

        assertThatThrownBy(() -> statusService.update(1L, new StatusRequest("inactive")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Status name already exists");
    }
}
