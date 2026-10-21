package edu.cibertec.account.service;

import edu.cibertec.account.dto.AccountFilterRequest;
import edu.cibertec.account.repository.AccountListRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountFilterTest {

    private final AccountListRepository repository = mock(AccountListRepository.class);
    private final AccountService service = new AccountService(null, repository, null, null, null);

    @Test
    void appliesDefaultsInServiceWhenQueryParametersAreAbsent() {
        var pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable, null, 0)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = service.findAll(new AccountFilterRequest(null, null, null, null));

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        verify(repository).findAll(pageable, null, 0);
    }

    @Test
    void validatesRangesInServiceBeforeQueryingDatabase() {
        var invalid = List.of(
                new AccountFilterRequest(-1, 20, null, 0),
                new AccountFilterRequest(0, 0, null, 0),
                new AccountFilterRequest(0, 101, null, 0),
                new AccountFilterRequest(0, 20, null, -1));
        invalid.forEach(filters -> assertThatThrownBy(() -> service.findAll(filters))
                .isInstanceOf(IllegalArgumentException.class));
        verifyNoInteractions(repository);
    }

    @Test
    void staffUsesTheRestrictedQueryAndServiceDefaults() {
        var pageable = PageRequest.of(0, 20);
        when(repository.findStaff(pageable, null, 0)).thenReturn(new PageImpl<>(List.of(), pageable, 0));
        var result = service.findStaff(new AccountFilterRequest(null, null, null, null));
        assertThat(result.size()).isEqualTo(20);
        verify(repository).findStaff(pageable, null, 0);
        verify(repository, never()).findAll(any(), any(), anyInt());
    }
}
