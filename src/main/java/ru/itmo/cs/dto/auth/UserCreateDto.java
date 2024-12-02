package ru.itmo.cs.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового пользователя.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для создания нового пользователя")
public class UserCreateDto {
    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Электронная почта пользователя", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;
}