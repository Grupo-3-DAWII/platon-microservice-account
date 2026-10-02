package edu.cibertec.account.dto;

public record AccountResponse(
        Long idUser,
        String name,
        String lastname,
        String username,
        String email,
        String nro,
        String direccion,
        Long idStatus,
        String status
) {
}
