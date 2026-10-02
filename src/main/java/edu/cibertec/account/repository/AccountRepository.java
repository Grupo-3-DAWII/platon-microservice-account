package edu.cibertec.account.repository;

import edu.cibertec.account.domain.entity.Account;
import org.springframework.data.repository.ListCrudRepository;

public interface AccountRepository extends ListCrudRepository<Account, Long> {

    boolean existsByUsername(String username);
}
