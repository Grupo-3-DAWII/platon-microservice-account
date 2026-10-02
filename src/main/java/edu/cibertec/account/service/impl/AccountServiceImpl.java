package edu.cibertec.account.service.impl;

import edu.cibertec.account.domain.entity.Account;
import edu.cibertec.account.domain.entity.Status;
import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.dto.PageResponse;
import edu.cibertec.account.exception.ResourceNotFoundException;
import edu.cibertec.account.exception.DuplicateResourceException;
import edu.cibertec.account.mapper.AccountMapper;
import edu.cibertec.account.repository.AccountRepository;
import edu.cibertec.account.repository.StatusRepository;
import edu.cibertec.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final StatusRepository statusRepository;
    private final AccountMapper accountMapper;

    @Override
    public PageResponse<AccountResponse> findAll(int page, int size, String search, Long idStatus) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("idUser").ascending());
        Page<Account> accountPage = findPage(normalizedSearch, idStatus, pageable);
        Page<AccountResponse> responsePage = accountPage.map(this::toResponse);

        return new PageResponse<>(responsePage.getContent(), responsePage.getNumber(),
                responsePage.getSize(), responsePage.getTotalElements(), responsePage.getTotalPages(),
                responsePage.isFirst(), responsePage.isLast());
    }

    private Page<Account> findPage(String search, Long idStatus, Pageable pageable) {
        if (search == null && idStatus == null) {
            return accountRepository.findAll(pageable);
        }
        if (search == null) {
            return accountRepository.findByIdStatus(idStatus, pageable);
        }
        String searchValue = search.toLowerCase();
        if (idStatus == null) {
            return accountRepository
                    .findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            searchValue, searchValue, searchValue, searchValue, pageable);
        }
        return accountRepository
                .findByIdStatusAndNameContainingIgnoreCaseOrIdStatusAndLastnameContainingIgnoreCaseOrIdStatusAndUsernameContainingIgnoreCaseOrIdStatusAndEmailContainingIgnoreCase(
                        idStatus, searchValue, idStatus, searchValue, idStatus, searchValue, idStatus, searchValue, pageable);
    }

    @Override
    public AccountResponse findById(Long id) {
        return toResponse(findAccount(id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Transactional
    @Override
    public AccountResponse create(AccountRequest request) {
        ensureUnique(request, null);
        Status status = findStatus(request.idStatus());

        Account account = accountMapper.toEntity(request, status.getIdStatus());

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    @Override
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = findAccount(id);
        ensureUnique(request, id);
        Status status = findStatus(request.idStatus());

        accountMapper.updateEntity(account, request, status.getIdStatus());

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    @Override
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

    private void ensureUnique(AccountRequest request, Long idUser) {
        boolean usernameExists = idUser == null
                ? accountRepository.existsByUsername(request.username())
                : accountRepository.existsByUsernameAndIdUserNot(request.username(), idUser);
        if (usernameExists) {
            throw new DuplicateResourceException("Username already exists");
        }

        boolean emailExists = idUser == null
                ? accountRepository.existsByEmail(request.email())
                : accountRepository.existsByEmailAndIdUserNot(request.email(), idUser);
        if (emailExists) {
            throw new DuplicateResourceException("Email already exists");
        }

        boolean nroExists = idUser == null
                ? accountRepository.existsByNro(request.nro())
                : accountRepository.existsByNroAndIdUserNot(request.nro(), idUser);
        if (nroExists) {
            throw new DuplicateResourceException("Phone number already exists");
        }
    }

    private AccountResponse toResponse(Account account) {
        Status status = findStatus(account.getIdStatus());
        return accountMapper.toResponse(account, status.getName());
    }
}
