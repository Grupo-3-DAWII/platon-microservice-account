package edu.cibertec.account.service;

import edu.cibertec.account.domain.entity.Role;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.CustomerRegistrationRequest;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.RoleRepository;
import edu.cibertec.account.repository.StatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {
    @Mock AccountService accounts;
    @Mock AccountRepository repository;
    @Mock RoleRepository roles;
    @Mock StatusRepository statuses;
    @InjectMocks CustomerRegistrationService service;

    private CustomerRegistrationRequest request(String name, String password) {
        return new CustomerRegistrationRequest(name, "Pérez", " Cliente ", " CLIENTE@EXAMPLE.TEST ",
                "900 000 009", " Calle de prueba 10 ", password);
    }

    @Test
    void usesTheServerCustomerRoleAndActiveStatusAndNormalizesInputs() {
        when(roles.findAll()).thenReturn(List.of(new Role(11L, "ADMIN"), new Role(92L, "USER"), new Role(37L, "SELLER")));
        when(statuses.findAll()).thenReturn(List.of(new Status(8L, "active"), new Status(9L, "inactive")));
        service.register(request(" María ", "Cliente123!"));
        var capture = ArgumentCaptor.forClass(AccountRequest.class);
        verify(accounts).create(capture.capture());
        var created = capture.getValue();
        assertEquals(92L, created.idRole()); assertEquals(8L, created.idStatus());
        assertEquals("María", created.name()); assertEquals("cliente", created.username());
        assertEquals("cliente@example.test", created.email()); assertEquals("900000009", created.nro());
        assertEquals("Cliente123!", created.password());
    }

    @Test
    void rejectsInvalidNamesAndPasswordsBeforeCreatingAnAccount() {
        assertThrows(IllegalArgumentException.class, () -> service.register(request("María123", "Cliente123!")));
        assertThrows(IllegalArgumentException.class, () -> service.register(request("María", "short")));
        assertThrows(IllegalArgumentException.class, () -> service.register(request("María", "á".repeat(37))));
        verifyNoInteractions(accounts);
    }

    @Test
    void rejectsDuplicateIdentityFields() {
        when(repository.hasUsername("cliente")).thenReturn(true);
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.register(request("María", "Cliente123!"))).getMessage().contains("usuario"));
        when(repository.hasUsername("cliente")).thenReturn(false);
        when(repository.hasEmail("cliente@example.test")).thenReturn(true);
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.register(request("María", "Cliente123!"))).getMessage().contains("correo"));
        when(repository.hasEmail("cliente@example.test")).thenReturn(false);
        when(repository.hasContact("900000009")).thenReturn(true);
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.register(request("María", "Cliente123!"))).getMessage().contains("contacto"));
        verifyNoInteractions(accounts);
    }

    @Test
    void neverFallsBackToAnAdministratorRoleWhenUserIsMissing() {
        when(roles.findAll()).thenReturn(List.of(new Role(11L, "ADMIN")));
        assertThrows(IllegalStateException.class, () -> service.register(request("María", "Cliente123!")));
        verifyNoInteractions(accounts);
    }
}
