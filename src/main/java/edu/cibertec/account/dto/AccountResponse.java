package edu.cibertec.account.dto;

import java.time.LocalDateTime;

public record AccountResponse(
        Long idUser,
        String name,
        String lastname,
        String username,
        Long idRole,
        String role,
        String email,
        String nro,
        String direccion,
        Long idStatus,
        String status,
        LocalDateTime createdAt
) {
}
