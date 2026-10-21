package edu.cibertec.account.service;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.domain.entity.Role;
import edu.cibertec.account.repository.RoleRepository;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountFilterRequest;
import edu.cibertec.account.dto.AccountListResponse;
import edu.cibertec.account.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.exception.ResourceNotFoundException;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.AccountListRepository;
import edu.cibertec.account.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountListRepository accountListRepository;
    private final StatusRepository statusRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<AccountListResponse> findAll(AccountFilterRequest filters) {
        return findAccounts(filters, false);
    }

    public PageResponse<AccountListResponse> findStaff(AccountFilterRequest filters) {
        return findAccounts(filters, true);
    }

    private PageResponse<AccountListResponse> findAccounts(AccountFilterRequest filters, boolean staffOnly) {
        int page = filters.page() == null ? 0 : filters.page();
        int size = filters.size() == null ? 20 : filters.size();
        int minPaidOrders = filters.minPaidOrders() == null ? 0 : filters.minPaidOrders();
        if (page < 0 || size < 1 || size > 100 || minPaidOrders < 0) {
            throw new IllegalArgumentException("page must be >= 0, size between 1 and 100, and minPaidOrders >= 0");
        }
        var pageable = PageRequest.of(page, size);
        var result = staffOnly
                ? accountListRepository.findStaff(pageable, filters.active(), minPaidOrders)
                : accountListRepository.findAll(pageable, filters.active(), minPaidOrders);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
    }

    public AccountResponse findById(Long id) {
        return toResponse(findAccount(id));
    }

    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        Role role = findRole(request.idRole());
        Status status = findStatus(request.idStatus());

        Account account = Account.builder()
                .name(request.name())
                .lastname(request.lastname())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .idRole(role.idRole())
                .idStatus(status.getIdStatus())
                .email(request.email())
                .nro(request.nro())
                .direccion(request.direccion())
                .createdAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .build();

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = findAccount(id);
        Status status = findStatus(request.idStatus());
        Role role = findRole(request.idRole());

        account.setName(request.name());
        account.setLastname(request.lastname());
        account.setUsername(request.username());
        account.setIdRole(role.idRole());
        if (request.password() != null && !request.password().isBlank()) {
            account.setPassword(passwordEncoder.encode(request.password()));
        }
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

    private Role findRole(Long idRole) {
        if (idRole == null) {
            throw new IllegalArgumentException("idRole is required");
        }
        return roleRepository.findById(idRole)
                .orElseThrow(() -> new IllegalArgumentException("Invalid idRole"));
    }

    private AccountResponse toResponse(Account account) {
        Status status = findStatus(account.getIdStatus());
        return new AccountResponse(
                account.getIdUser(),
                account.getName(),
                account.getLastname(),
                account.getUsername(),
                account.getIdRole(),
                findRole(account.getIdRole()).name(),
                account.getEmail(),
                account.getNro(),
                account.getDireccion(),
                account.getIdStatus(),
                status.getName(),
                account.getCreatedAt()
        );
    }
}
