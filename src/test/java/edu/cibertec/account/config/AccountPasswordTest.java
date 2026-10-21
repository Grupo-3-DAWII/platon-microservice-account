package edu.cibertec.account.config;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.domain.entity.Role;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.repository.AccountListRepository;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.RoleRepository;
import edu.cibertec.account.repository.StatusRepository;
import edu.cibertec.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountPasswordTest {

    private final AccountRepository accounts = mock(AccountRepository.class);
    private final StatusRepository statuses = mock(StatusRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final PasswordEncoder encoder = new SecurityConfig().passwordEncoder();
    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accounts, mock(AccountListRepository.class), statuses, roles, encoder);
        when(statuses.findById(1L)).thenReturn(Optional.of(new Status(1L, "active")));
        when(roles.findById(2L)).thenReturn(Optional.of(new Role(2L, "USER")));
        when(accounts.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void creationPersistsBcryptHashCompatibleWithGateway() {
        service.create(request("Secret123"));

        var saved = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(saved.capture());
        String hash = saved.getValue().getPassword();
        assertThat(hash).startsWith("{bcrypt}$2").isNotEqualTo("Secret123");
        assertThat(new BCryptPasswordEncoder().matches("Secret123", hash.substring("{bcrypt}".length())))
                .isTrue();
        // The gateway uses the same delegating encoder factory.
        assertThat(PasswordEncoderFactories.createDelegatingPasswordEncoder().matches("Secret123", hash))
                .isTrue();
        assertThat(encoder.matches("WrongPassword", hash)).isFalse();
        assertThat(encoder.encode("Secret123")).isNotEqualTo(hash);
    }

    @Test
    void passwordChangePersistsNewHashAndInvalidatesPreviousPassword() {
        var account = Account.builder().idUser(10L).password(encoder.encode("OldSecret123")).build();
        when(accounts.findById(10L)).thenReturn(Optional.of(account));

        service.update(10L, request("NewSecret123"));

        assertThat(account.getPassword()).startsWith("{bcrypt}$2");
        assertThat(encoder.matches("NewSecret123", account.getPassword())).isTrue();
        assertThat(encoder.matches("OldSecret123", account.getPassword())).isFalse();
    }

    @Test
    void editingWithoutPasswordPreservesExistingHash() {
        String hash = encoder.encode("Secret123");
        var account = Account.builder().idUser(10L).password(hash).build();
        when(accounts.findById(10L)).thenReturn(Optional.of(account));

        service.update(10L, request(null));
        assertThat(account.getPassword()).isEqualTo(hash);
        service.update(10L, request(" "));
        assertThat(account.getPassword()).isEqualTo(hash);
    }

    private AccountRequest request(String password) {
        return new AccountRequest("Ana", "Torres", "ana", password, 2L,
                "ana@demo.com", "999888777", "Av. Lima 123", 1L);
    }
}
