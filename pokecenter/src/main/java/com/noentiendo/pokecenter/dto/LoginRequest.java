package com.noentiendo.pokecenter.dto;

public record LoginRequest(
    String username,
    String password
) {
}
