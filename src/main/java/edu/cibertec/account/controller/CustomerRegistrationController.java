package edu.cibertec.account.controller;

import edu.cibertec.account.dto.CustomerRegistrationRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.service.CustomerRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/register")
@RequiredArgsConstructor
public class CustomerRegistrationController {
    private final CustomerRegistrationService registrations;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(@RequestBody CustomerRegistrationRequest request) {
        return registrations.register(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail duplicate(DataIntegrityViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Ya existe una cuenta con esos datos.");
    }
}
