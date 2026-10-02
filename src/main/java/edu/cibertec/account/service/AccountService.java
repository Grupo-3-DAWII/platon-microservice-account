package edu.cibertec.account.service;

import edu.cibertec.account.dto.AccountRequest;
import edu.cibertec.account.dto.AccountResponse;
import edu.cibertec.account.dto.PageResponse;

public interface AccountService {

    PageResponse<AccountResponse> findAll(int page, int size, String search, Long idStatus);

    AccountResponse findById(Long id);

    boolean existsByUsername(String username);

    AccountResponse create(AccountRequest request);

    AccountResponse update(Long id, AccountRequest request);

    void delete(Long id);
}
