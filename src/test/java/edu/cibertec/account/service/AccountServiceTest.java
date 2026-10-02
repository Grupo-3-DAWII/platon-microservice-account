package edu.cibertec.account.service;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.dto.PageResponse;
import edu.cibertec.account.exception.DuplicateResourceException;
import edu.cibertec.account.exception.ResourceNotFoundException;
import edu.cibertec.account.mapper.AccountMapper;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.StatusRepository;
import edu.cibertec.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StatusRepository statusRepository;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldCreateAccountWhenUnique() {
        AccountRequest request = validRequest();
        Status status = Status.builder().idStatus(1L).name("active").build();
        Account saved = Account.builder().idUser(10L).name("Ana").lastname("Torres")
                .username("atorres").idStatus(1L).email("ana@demo.com")
                .nro("999888777").direccion("Av. Lima 123").build();

        when(statusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        assertThat(accountService.create(request).idUser()).isEqualTo(10L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldFindAllAccounts() {
        Account first = account(10L, "atorres", 1L);
        Account second = account(11L, "bgarcia", 2L);
        when(accountRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(first, second), PageRequest.of(0, 20), 2));
        when(statusRepository.findById(1L)).thenReturn(Optional.of(status(1L, "active")));
        when(statusRepository.findById(2L)).thenReturn(Optional.of(status(2L, "inactive")));

        PageResponse<AccountResponse> page = accountService.findAll(0, 20, null, null);

        assertThat(page.content()).hasSize(2)
                .extracting(AccountResponse::username)
                .containsExactly("atorres", "bgarcia");
        assertThat(page.totalElements()).isEqualTo(2L);
        assertThat(page.first()).isTrue();
        assertThat(page.last()).isTrue();
    }

    @Test
    void shouldFindAccountById() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account(10L, "atorres", 1L)));
        when(statusRepository.findById(1L)).thenReturn(Optional.of(status(1L, "active")));

        assertThat(accountService.findById(10L).username()).isEqualTo("atorres");
    }

    @Test
    void shouldReportUsernameExistence() {
        when(accountRepository.existsByUsername("atorres")).thenReturn(true);

        assertThat(accountService.existsByUsername("atorres")).isTrue();
    }

    @Test
    void shouldNormalizeSearchAndBuildNonLastPage() {
        when(accountRepository
                .findByIdStatusAndNameContainingIgnoreCaseOrIdStatusAndLastnameContainingIgnoreCaseOrIdStatusAndUsernameContainingIgnoreCaseOrIdStatusAndEmailContainingIgnoreCase(
                        eq(1L), eq("ana"), eq(1L), eq("ana"), eq(1L), eq("ana"), eq(1L), eq("ana"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 20), 41));

        PageResponse<AccountResponse> page = accountService.findAll(1, 20, "  ana  ", 1L);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isFalse();
    }

    @Test
    void shouldFilterByStatus() {
        when(accountRepository.findByIdStatus(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        assertThat(accountService.findAll(0, 20, null, 1L).totalElements()).isZero();
    }

    @Test
    void shouldFilterBySearch() {
        when(accountRepository
                .findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        eq("ana"), eq("ana"), eq("ana"), eq("ana"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        assertThat(accountService.findAll(0, 20, "ana", null).totalElements()).isZero();
    }

    @Test
    void shouldRejectInvalidPage() {
        assertThatThrownBy(() -> accountService.findAll(-1, 20, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> accountService.findAll(0, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> accountService.findAll(0, 101, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUpdateAccountWhenUnique() {
        Account account = account(10L, "atorres", 1L);
        AccountRequest request = new AccountRequest("Ana Updated", "Torres", "ana.updated",
                "updated@demo.com", "999000111", "Av. Nueva 1", 2L);
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));
        when(statusRepository.findById(2L)).thenReturn(Optional.of(status(2L, "inactive")));
        when(accountRepository.save(account)).thenReturn(account);

        assertThat(accountService.update(10L, request).username()).isEqualTo("ana.updated");
        assertThat(account.getName()).isEqualTo("Ana Updated");
        verify(accountRepository).existsByUsernameAndIdUserNot("ana.updated", 10L);
        verify(accountRepository).existsByEmailAndIdUserNot("updated@demo.com", 10L);
        verify(accountRepository).existsByNroAndIdUserNot("999000111", 10L);
    }

    @Test
    void shouldDeleteAccount() {
        Account account = account(10L, "atorres", 1L);
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        accountService.delete(10L);

        verify(accountRepository).delete(account);
    }

    @Test
    void shouldRejectMissingAccount() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");
    }

    @Test
    void shouldRejectMissingStatus() {
        when(statusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.create(validRequestWithStatus(99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Status not found");
    }

    @Test
    void shouldRejectDuplicateUsername() {
        AccountRequest request = validRequest();
        when(accountRepository.existsByUsername("atorres")).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists");

        verify(statusRepository, never()).findById(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(accountRepository.existsByEmail("ana@demo.com")).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void shouldRejectDuplicatePhoneNumber() {
        when(accountRepository.existsByNro("999888777")).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone number already exists");
    }

    @Test
    void shouldRejectDuplicateUsernameOnUpdate() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account(10L, "atorres", 1L)));
        when(accountRepository.existsByUsernameAndIdUserNot("atorres", 10L)).thenReturn(true);

        assertThatThrownBy(() -> accountService.update(10L, validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void shouldRejectDuplicateEmailOnUpdate() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account(10L, "atorres", 1L)));
        when(accountRepository.existsByEmailAndIdUserNot("ana@demo.com", 10L)).thenReturn(true);

        assertThatThrownBy(() -> accountService.update(10L, validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void shouldRejectDuplicatePhoneNumberOnUpdate() {
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account(10L, "atorres", 1L)));
        when(accountRepository.existsByNroAndIdUserNot("999888777", 10L)).thenReturn(true);

        assertThatThrownBy(() -> accountService.update(10L, validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone number already exists");
    }

    @Test
    void shouldRejectMissingAccountOnUpdateAndDelete() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.update(99L, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> accountService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private AccountRequest validRequest() {
        return validRequestWithStatus(1L);
    }

    private AccountRequest validRequestWithStatus(Long idStatus) {
        return new AccountRequest("Ana", "Torres", "atorres", "ana@demo.com",
                "999888777", "Av. Lima 123", idStatus);
    }

    private Account account(Long id, String username, Long idStatus) {
        return Account.builder().idUser(id).name("Ana").lastname("Torres")
                .username(username).idStatus(idStatus).email(username + "@demo.com")
                .nro("999888777").direccion("Av. Lima 123").build();
    }

    private Status status(Long id, String name) {
        return Status.builder().idStatus(id).name(name).build();
    }
}
