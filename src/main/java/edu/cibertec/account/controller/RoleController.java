package edu.cibertec.account.controller;

import edu.cibertec.account.domain.entity.Role;
import edu.cibertec.account.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public List<Role> findAll() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::idRole)).toList();
    }
}
