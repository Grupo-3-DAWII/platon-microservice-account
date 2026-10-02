package edu.cibertec.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusRequest(
        @NotBlank
        @Size(max = 20)
        String name
) {
}
