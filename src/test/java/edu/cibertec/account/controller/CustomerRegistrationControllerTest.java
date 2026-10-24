package edu.cibertec.account.controller;

import edu.cibertec.account.dto.CustomerRegistrationRequest;
import edu.cibertec.account.service.CustomerRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerRegistrationController.class)
class CustomerRegistrationControllerTest {
    @Autowired MockMvc mvc;
    @MockBean CustomerRegistrationService registrations;

    @Test
    void acceptsOnlyCustomerFieldsEvenWhenPrivilegeFieldsAreSubmitted() throws Exception {
        mvc.perform(post("/api/accounts/register").contentType("application/json").content("""
                {"name":"Cliente","lastname":"Prueba","username":"cliente","password":"Cliente123!",
                 "email":"cliente@example.test","nro":"900000009","direccion":"Calle 10","idRole":1,"idStatus":2}
                """)).andExpect(status().isCreated());
        verify(registrations).register(new CustomerRegistrationRequest("Cliente", "Prueba", "cliente",
                "cliente@example.test", "900000009", "Calle 10", "Cliente123!"));
    }

    @Test
    void mapsConcurrentDuplicatesToAReadableConflict() throws Exception {
        when(registrations.register(any())).thenThrow(new DataIntegrityViolationException("private database detail"));
        mvc.perform(post("/api/accounts/register").contentType("application/json").content("{}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("Ya existe una cuenta con esos datos."));
    }
}
