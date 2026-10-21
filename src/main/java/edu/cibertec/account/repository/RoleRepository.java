package edu.cibertec.account.repository;

import edu.cibertec.account.domain.entity.Role;
import org.springframework.data.repository.ListCrudRepository;

public interface RoleRepository extends ListCrudRepository<Role, Long> {
}
