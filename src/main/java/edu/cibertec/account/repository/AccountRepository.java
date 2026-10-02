package edu.cibertec.account.repository;

import edu.cibertec.account.domain.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AccountRepository extends ListCrudRepository<Account, Long>, PagingAndSortingRepository<Account, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNro(String nro);

    boolean existsByUsernameAndIdUserNot(String username, Long idUser);

    boolean existsByEmailAndIdUserNot(String email, Long idUser);

    boolean existsByNroAndIdUserNot(String nro, Long idUser);

    Page<Account> findByIdStatus(Long idStatus, Pageable pageable);

    Page<Account> findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String lastname, String username, String email, Pageable pageable);

    Page<Account> findByIdStatusAndNameContainingIgnoreCaseOrIdStatusAndLastnameContainingIgnoreCaseOrIdStatusAndUsernameContainingIgnoreCaseOrIdStatusAndEmailContainingIgnoreCase(
            Long idStatusForName, String name,
            Long idStatusForLastname, String lastname,
            Long idStatusForUsername, String username,
            Long idStatusForEmail, String email,
            Pageable pageable);
}
