package com.noentiendo.pokecenter.dto;

public record AuthResponse(
    String token,
    long expiresIn,
    String username
) {
}
