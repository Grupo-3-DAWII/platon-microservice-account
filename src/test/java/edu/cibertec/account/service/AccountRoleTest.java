package edu.cibertec.account.service;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.domain.entity.Role;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.repository.AccountListRepository;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.RoleRepository;
import edu.cibertec.account.repository.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountRoleTest {

    private final AccountRepository accounts = mock(AccountRepository.class);
    private final StatusRepository statuses = mock(StatusRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accounts, mock(AccountListRepository.class), statuses, roles, encoder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "USER", "SELLER"})
    void createsAndUpdatesAccountsWithEachRole(String name) {
        // IDs come from the catalog; SELLER need not have ID 3.
        long idRole = switch (name) { case "ADMIN" -> 1L; case "USER" -> 2L; default -> 5L; };
        when(roles.findById(idRole)).thenReturn(Optional.of(new Role(idRole, name)));
        when(statuses.findById(1L)).thenReturn(Optional.of(new Status(1L, "active")));
        when(encoder.encode("secret")).thenReturn("encoded");
        when(accounts.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));

        var created = service.create(request(idRole));
        assertThat(created.idRole()).isEqualTo(idRole);
        assertThat(created.role()).isEqualTo(name);
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.createdAt().getNano() % 1000).isZero();

        var originalDate = LocalDateTime.of(2026, 1, 15, 9, 30);
        when(accounts.findById(10L)).thenReturn(Optional.of(Account.builder()
                .idUser(10L).createdAt(originalDate).build()));
        var updated = service.update(10L, request(idRole));
        assertThat(updated.idRole()).isEqualTo(idRole);
        assertThat(updated.role()).isEqualTo(name);
        assertThat(updated.createdAt()).isEqualTo(originalDate);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {999L})
    void rejectsMissingOrUnknownRolesBeforeSaving(Long idRole) {
        assertThatThrownBy(() -> service.create(request(idRole)))
                .isInstanceOf(IllegalArgumentException.class);
        when(accounts.findById(10L)).thenReturn(Optional.of(Account.builder().idUser(10L).build()));
        when(statuses.findById(1L)).thenReturn(Optional.of(new Status(1L, "active")));
        assertThatThrownBy(() -> service.update(10L, request(idRole)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(accounts, never()).save(any());
    }

    private AccountRequest request(Long idRole) {
        return new AccountRequest("Ana", "Torres", "ana", "secret", idRole,
                "ana@demo.com", "999888777", "Av. Lima 123", 1L);
    }
}
