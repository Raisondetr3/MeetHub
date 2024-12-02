package ru.itmo.cs.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на аутентификацию.
 */
@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private long expirationTime;
    private UserDto user;
}
