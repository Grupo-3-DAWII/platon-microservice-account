package edu.cibertec.account.controller;

import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountFilterRequest;
import edu.cibertec.account.dto.AccountListResponse;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.dto.PageResponse;
import edu.cibertec.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PutMapping("/me/contact")
    public AccountResponse updateContact(@org.springframework.web.bind.annotation.RequestHeader("X-Platon-User-Id") Long userId,
            @RequestBody edu.cibertec.account.dto.ContactRequest request) {
        return accountService.updateContact(userId, request);
    }

    @GetMapping
    public PageResponse<AccountListResponse> findAll(@ModelAttribute AccountFilterRequest filters) {
        return accountService.findAll(filters);
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @GetMapping("/staff")
    public PageResponse<AccountListResponse> findStaff(@ModelAttribute AccountFilterRequest filters) {
        return accountService.findStaff(filters);
    }

    @GetMapping("/validate/{username}")
    public boolean existsByUsername(@PathVariable String username) {
        return accountService.existsByUsername(username);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request));
    }

    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable Long id, @RequestBody AccountRequest request) {
        return accountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
