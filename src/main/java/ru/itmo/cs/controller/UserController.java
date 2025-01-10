package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.cs.dto.auth.AuthResponseDto;
import ru.itmo.cs.dto.auth.LoginRequestDto;
import ru.itmo.cs.dto.auth.UserCreateDto;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.UserService;

/**
 * Контроллер для работы с пользователями.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Эндпоинты для аутентификации пользователей")
public class UserController {

    private final UserService userService;

    /**
     * Регистрация нового пользователя.
     *
     * @param userCreateDto данные для создания пользователя
     * @return ответ с токеном, временем истечения и данными пользователя
     */
    @Operation(summary = "Регистрация нового пользователя",
        description = "Создает нового пользователя и возвращает токен с его данными.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
        @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.register(userCreateDto));
    }

    /**
     * Вход в систему.
     *
     * @param loginRequestDto данные для входа
     * @return ответ с токеном, временем истечения и данными пользователя
     */
    @Operation(summary = "Вход в систему",
        description = "Аутентифицирует пользователя и возвращает токен с его данными.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
        @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(userService.login(loginRequestDto));
    }

    public User getCurrentUser() {
        return userService.me();
    }

}
