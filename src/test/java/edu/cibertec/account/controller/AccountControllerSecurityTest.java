package edu.cibertec.account.controller;

import edu.cibertec.account.dto.AccountFilterRequest;
import edu.cibertec.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import edu.cibertec.account.dto.PageResponse;
import java.util.List;

@WebMvcTest(AccountController.class)
class AccountControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void shouldAllowAnonymousRead() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk());
        verify(accountService).findAll(new AccountFilterRequest(null, null, null, null));
    }

    @Test
    void bindsPaginationAndCombinedFiltersAndReturnsPageMetadata() throws Exception {
        when(accountService.findAll(new AccountFilterRequest(1, 10, false, 5)))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false, true));
        mockMvc.perform(get("/api/accounts")
                        .param("page", "1").param("size", "10")
                        .param("active", "false").param("minPaidOrders", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0));
        verify(accountService).findAll(new AccountFilterRequest(1, 10, false, 5));
    }

    @Test
    void rejectsMalformedFilters() throws Exception {
        mockMvc.perform(get("/api/accounts").param("active", "invalid"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/accounts").param("minPaidOrders", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void staffRouteDelegatesFiltersWithoutBeingTreatedAsAnAccountId() throws Exception {
        var filters = new AccountFilterRequest(0, 10, true, 5);
        when(accountService.findStaff(filters))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));
        mockMvc.perform(get("/api/accounts/staff")
                        .param("page", "0").param("size", "10")
                        .param("active", "true").param("minPaidOrders", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
        verify(accountService).findStaff(filters);
    }
}
