package ru.itmo.cs.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для входа в систему.
 */
@Data
@AllArgsConstructor
public class LoginRequestDto {
    private String username;
    private String password;
}
