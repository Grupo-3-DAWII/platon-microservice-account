package edu.cibertec.account.service;
import edu.cibertec.account.domain.entity.*;
import edu.cibertec.account.dto.ContactRequest;
import edu.cibertec.account.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ContactUpdateTest {
    @Test
    void updatesOnlyContactAndPreservesIdentityAndAccess() {
        var accounts=mock(AccountRepository.class); var statuses=mock(StatusRepository.class); var roles=mock(RoleRepository.class);
        var account=Account.builder().idUser(2L).name("Elena").lastname("Soto").username("elenasoto")
            .email("elena@example.test").password("encoded").idRole(2L).idStatus(1L).nro("900000021").direccion("Calle 21").build();
        when(accounts.findById(2L)).thenReturn(Optional.of(account));
        when(accounts.save(any())).thenAnswer(call->call.getArgument(0));
        when(statuses.findById(1L)).thenReturn(Optional.of(new Status(1L,"active")));
        when(roles.findById(2L)).thenReturn(Optional.of(new Role(2L,"USER")));
        var service=new AccountService(accounts,mock(AccountListRepository.class),statuses,roles,mock(PasswordEncoder.class));
        service.updateContact(2L,new ContactRequest("900 000 021","  Avenida   Lima 25  "));
        assertEquals("900000021",account.getNro()); assertEquals("Avenida Lima 25",account.getDireccion());
        assertEquals("elenasoto",account.getUsername()); assertEquals("encoded",account.getPassword());
        assertEquals(2L,account.getIdRole()); assertEquals(1L,account.getIdStatus()); assertEquals("elena@example.test",account.getEmail());
        assertThrows(IllegalArgumentException.class,()->service.updateContact(2L,new ContactRequest("abc123","Calle 10")));
        assertThrows(IllegalArgumentException.class,()->service.updateContact(2L,new ContactRequest("900000021","   ")));
        when(accounts.hasContact("900000099")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,()->service.updateContact(2L,new ContactRequest("900000099","Calle 10")));
        verify(accounts,times(1)).save(account);
    }
}
