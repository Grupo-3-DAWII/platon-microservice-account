package edu.cibertec.account.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "roles", schema = "account")
public record Role(@Id @Column("idrole") Long idRole, String name) {
}
