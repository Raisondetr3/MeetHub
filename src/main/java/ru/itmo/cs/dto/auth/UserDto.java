package ru.itmo.cs.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для передачи данных пользователя")
public class UserDto {
    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Integer id;

    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Электронная почта пользователя", example = "john.doe@example.com")
    private String email;
}