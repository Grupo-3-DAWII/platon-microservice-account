package edu.cibertec.account.service;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.exception.ResourceNotFoundException;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final StatusRepository statusRepository;

    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AccountResponse findById(Long id) {
        return toResponse(findAccount(id));
    }

    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        Status status = findStatus(request.idStatus());

        Account account = Account.builder()
                .name(request.name())
                .lastname(request.lastname())
                .username(request.username())
                .idStatus(status.getIdStatus())
                .email(request.email())
                .nro(request.nro())
                .direccion(request.direccion())
                .build();

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = findAccount(id);
        Status status = findStatus(request.idStatus());

        account.setName(request.name());
        account.setLastname(request.lastname());
        account.setUsername(request.username());
        account.setIdStatus(status.getIdStatus());
        account.setEmail(request.email());
        account.setNro(request.nro());
        account.setDireccion(request.direccion());

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(Long id) {
        Account account = findAccount(id);
        accountRepository.delete(account);
    }

    private Account findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Status findStatus(Long idStatus) {
        return statusRepository.findById(idStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
    }

    private AccountResponse toResponse(Account account) {
        Status status = findStatus(account.getIdStatus());
        return new AccountResponse(
                account.getIdUser(),
                account.getName(),
                account.getLastname(),
                account.getUsername(),
                account.getEmail(),
                account.getNro(),
                account.getDireccion(),
                account.getIdStatus(),
                status.getName()
        );
    }
}
