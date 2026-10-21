package edu.cibertec.account.dto;

public record AccountFilterRequest(
        Integer page,
        Integer size,
        Boolean active,
        Integer minPaidOrders
) {
}
