package com.example.accounting.infrastructure.web.dto;

public record LoginResponse(
    String token,
    String refreshToken,
    String email,
    String name,
    String role
) {
}
