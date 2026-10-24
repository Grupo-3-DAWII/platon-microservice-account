package edu.cibertec.account.repository;

import edu.cibertec.account.domain.entity.Account;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends ListCrudRepository<Account, Long> {

    boolean existsByUsername(String username);

    @Query("SELECT COUNT(*) > 0 FROM account.users WHERE LOWER(username) = LOWER(:value)")
    boolean hasUsername(@Param("value") String value);

    @Query("SELECT COUNT(*) > 0 FROM account.users WHERE LOWER(email) = LOWER(:value)")
    boolean hasEmail(@Param("value") String value);

    @Query("SELECT COUNT(*) > 0 FROM account.users WHERE REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(nro, ' ', ''), '+', ''), '(', ''), ')', ''), '-', '') = :value")
    boolean hasContact(@Param("value") String value);
}
