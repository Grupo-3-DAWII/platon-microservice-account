package edu.cibertec.account.repository;

import edu.cibertec.account.domain.entity.Status;
import org.springframework.data.repository.ListCrudRepository;

public interface StatusRepository extends ListCrudRepository<Status, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdStatusNot(String name, Long idStatus);
}
