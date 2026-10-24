package edu.cibertec.account.dto;

public record CustomerRegistrationRequest(String name, String lastname, String username,
        String email, String nro, String direccion, String password) {
}
