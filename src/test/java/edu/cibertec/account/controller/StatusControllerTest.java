package edu.cibertec.account.controller;

import edu.cibertec.account.config.SecurityConfig;
import edu.cibertec.account.config.WebConfig;
import edu.cibertec.account.dto.StatusResponse;
import edu.cibertec.account.dto.StatusRequest;
import edu.cibertec.account.service.StatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatusController.class)
@Import({SecurityConfig.class, WebConfig.class})
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatusService statusService;

    @Test
    void shouldRejectAnonymousStatusRead() throws Exception {
        mockMvc.perform(get("/api/statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnStatusesForAuthenticatedUser() throws Exception {
        when(statusService.findAll()).thenReturn(List.of(new StatusResponse(1L, "active")));

        mockMvc.perform(get("/api/statuses").with(httpBasic("student", "student123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatusByIdForAuthenticatedUser() throws Exception {
        when(statusService.findById(1L)).thenReturn(new StatusResponse(1L, "active"));

        mockMvc.perform(get("/api/statuses/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminToCreateUpdateAndDeleteStatus() throws Exception {
        when(statusService.create(any(StatusRequest.class))).thenReturn(new StatusResponse(3L, "pending"));
        when(statusService.update(3L, new StatusRequest("enabled")))
                .thenReturn(new StatusResponse(3L, "enabled"));
        doNothing().when(statusService).delete(3L);

        mockMvc.perform(post("/api/statuses").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content("{\"name\":\"pending\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/statuses/3").with(httpBasic("admin", "admin123"))
                        .contentType(APPLICATION_JSON).content("{\"name\":\"enabled\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/statuses/3").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectUserStatusWriteOperations() throws Exception {
        mockMvc.perform(post("/api/statuses").with(httpBasic("student", "student123"))
                        .contentType(APPLICATION_JSON).content("{\"name\":\"pending\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/statuses/1").with(httpBasic("student", "student123")))
                .andExpect(status().isForbidden());
    }
}
