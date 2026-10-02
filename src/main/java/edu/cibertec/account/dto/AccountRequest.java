package edu.cibertec.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @NotBlank @Size(max = 80)
        String name,
        @NotBlank @Size(max = 80)
        String lastname,
        @NotBlank @Size(max = 80)
        String username,
        @NotBlank @Email @Size(max = 120)
        String email,
        @NotBlank @Size(max = 20)
        String nro,
        @NotBlank @Size(max = 180)
        String direccion,
        @NotNull
        Long idStatus
) {
}
