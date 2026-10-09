package edu.cibertec.account.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "account")
public class Account {

    @Id
    @Column("iduser")
    private Long idUser;

    @Column("name")
    private String name;

    @Column("lastname")
    private String lastname;

    @Column("username")
    private String username;

    @Column("password")
    private String password;

    @Column("idrole")
    private Long idRole;

    @Column("idstatus")
    private Long idStatus;

    @Column("email")
    private String email;

    @Column("nro")
    private String nro;

    @Column("direccion")
    private String direccion;
}
