package edu.cibertec.account.dto;

public record AccountRequest(
        String name,
        String lastname,
        String username,
        String email,
        String nro,
        String direccion,
        Long idStatus
) {
}
