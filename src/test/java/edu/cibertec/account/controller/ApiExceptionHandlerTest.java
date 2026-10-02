package edu.cibertec.account.controller;

import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.exception.DuplicateResourceException;
import edu.cibertec.account.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldHandleNotFound() {
        ProblemDetail result = handler.handleNotFound(new ResourceNotFoundException("missing"), request());

        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getDetail()).isEqualTo("missing");
    }

    @Test
    void shouldHandleDuplicate() {
        ProblemDetail result = handler.handleDuplicate(new DuplicateResourceException("duplicate"), request());

        assertThat(result.getStatus()).isEqualTo(409);
        assertThat(result.getDetail()).isEqualTo("duplicate");
    }

    @Test
    void shouldHandleBadRequest() {
        ProblemDetail result = handler.handleBadRequest(new IllegalArgumentException("invalid"), request());

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo("invalid");
    }

    @Test
    void shouldHandleDataIntegrity() {
        ProblemDetail result = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate"), request());

        assertThat(result.getStatus()).isEqualTo(409);
        assertThat(result.getDetail()).isEqualTo("Database constraint violation");
    }

    @Test
    void shouldHandleValidationFields() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("accountRequest", "name", "must not be blank"),
                new FieldError("accountRequest", "email", "must be a well-formed email address")));

        ProblemDetail result = handler.handleValidation(validationException(bindingResult), request());

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).contains("name: must not be blank")
                .contains("email: must be a well-formed email address");
    }

    @Test
    void shouldHandleValidationWithoutFields() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ProblemDetail result = handler.handleValidation(validationException(bindingResult), request());

        assertThat(result.getDetail()).isEqualTo("Invalid request");
    }

    private MethodArgumentNotValidException validationException(BindingResult bindingResult) throws Exception {
        Method method = AccountController.class.getMethod("create", AccountRequest.class);
        return new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        return request;
    }
}
