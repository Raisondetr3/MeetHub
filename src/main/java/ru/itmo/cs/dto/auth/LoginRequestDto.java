package ru.itmo.cs.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для входа в систему.
 */
@Data
@AllArgsConstructor
@Schema(description = "DTO для входа в систему")
public class LoginRequestDto {
    @Schema(description = "Имя пользователя для входа", example = "john_doe")
    private String username;

    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;
}