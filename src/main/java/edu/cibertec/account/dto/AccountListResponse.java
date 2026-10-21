package edu.cibertec.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountListResponse(
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
        long paidOrderCount,
        BigDecimal totalPurchases,
        LocalDateTime createdAt
) {
}
