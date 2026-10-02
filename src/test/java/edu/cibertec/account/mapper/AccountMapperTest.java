package edu.cibertec.account.mapper;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void shouldMapRequestToEntity() {
        Account account = mapper.toEntity(request(), 2L);

        assertThat(account.getName()).isEqualTo("Ana");
        assertThat(account.getUsername()).isEqualTo("atorres");
        assertThat(account.getIdStatus()).isEqualTo(2L);
        assertThat(account.getEmail()).isEqualTo("ana@demo.com");
    }

    @Test
    void shouldUpdateEntityFromRequest() {
        Account account = Account.builder().idUser(10L).build();

        mapper.updateEntity(account, request(), 1L);

        assertThat(account.getIdUser()).isEqualTo(10L);
        assertThat(account.getName()).isEqualTo("Ana");
        assertThat(account.getIdStatus()).isEqualTo(1L);
    }

    @Test
    void shouldMapEntityToResponse() {
        Account account = Account.builder().idUser(10L).name("Ana").lastname("Torres")
                .username("atorres").email("ana@demo.com").nro("999888777")
                .direccion("Av. Lima 123").idStatus(1L).build();

        AccountResponse response = mapper.toResponse(account, "active");

        assertThat(response.idUser()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo("active");
    }

    private AccountRequest request() {
        return new AccountRequest("Ana", "Torres", "atorres", "ana@demo.com",
                "999888777", "Av. Lima 123", 1L);
    }
}
