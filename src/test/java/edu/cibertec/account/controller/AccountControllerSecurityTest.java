package edu.cibertec.account.controller;

import edu.cibertec.account.config.SecurityConfig;
import edu.cibertec.account.config.WebConfig;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.dto.PageResponse;
import edu.cibertec.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, WebConfig.class})
class AccountControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void shouldRejectAnonymousRead() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUserToReadAccounts() throws Exception {
        when(accountService.findAll(0, 20, null, null))
                .thenReturn(new PageResponse<>(List.of(response()), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/accounts").with(httpBasic("student", "student123")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"content\":[{\"idUser\":1,\"name\":\"Ana\",\"lastname\":\"Torres\",\"username\":\"atorres\",\"email\":\"ana@demo.com\",\"nro\":\"999888777\",\"direccion\":\"Av. Lima 123\",\"idStatus\":1,\"status\":\"active\"}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true}"));
    }

    @Test
    void shouldAllowAdminToReadAccountByIdAndValidateUsername() throws Exception {
        when(accountService.findById(1L)).thenReturn(response());
        when(accountService.existsByUsername("atorres")).thenReturn(true);

        mockMvc.perform(get("/api/accounts/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/accounts/validate/atorres").with(httpBasic("student", "student123")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldAllowAdminToCreateAndUpdateAccount() throws Exception {
        String body = "{\"name\":\"Ana\",\"lastname\":\"Torres\",\"username\":\"atorres\",\"email\":\"ana@demo.com\",\"nro\":\"999888777\",\"direccion\":\"Av. Lima 123\",\"idStatus\":1}";
        when(accountService.create(any())).thenReturn(response());
        when(accountService.update(any(), any())).thenReturn(response());

        mockMvc.perform(post("/api/accounts").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/accounts/1").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/accounts").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUserWriteOperations() throws Exception {
        mockMvc.perform(post("/api/accounts").with(httpBasic("student", "student123"))
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/accounts/1").with(httpBasic("student", "student123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteAccount() throws Exception {
        doNothing().when(accountService).delete(1L);

        mockMvc.perform(delete("/api/accounts/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    private AccountResponse response() {
        return new AccountResponse(1L, "Ana", "Torres", "atorres", "ana@demo.com",
                "999888777", "Av. Lima 123", 1L, "active");
    }

}
