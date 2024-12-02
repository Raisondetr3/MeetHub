package ru.itmo.cs.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на аутентификацию.
 */
@Data
@AllArgsConstructor
@Schema(description = "DTO для ответа на аутентификацию")
public class AuthResponseDto {
    @Schema(description = "JWT токен для аутентификации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Время истечения токена в миллисекундах с момента создания", example = "1693658745123")
    private long expirationTime;

    @Schema(description = "Данные пользователя, полученные после аутентификации")
    private UserDto user;
}