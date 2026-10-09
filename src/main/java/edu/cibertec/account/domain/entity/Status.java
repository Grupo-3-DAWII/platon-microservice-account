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
@Table(name = "status", schema = "account")
public class Status {

    @Id
    @Column("idstatus")
    private Long idStatus;

    @Column("name")
    private String name;
}
