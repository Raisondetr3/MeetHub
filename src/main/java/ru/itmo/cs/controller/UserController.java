package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.cs.dto.auth.AuthResponseDto;
import ru.itmo.cs.dto.auth.LoginRequestDto;
import ru.itmo.cs.dto.auth.UserCreateDto;
import ru.itmo.cs.service.UserService;

/**
 * Контроллер для работы с пользователями.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Регистрация нового пользователя.
     *
     * @param userCreateDto данные для создания пользователя
     * @return ответ с токеном, временем истечения и данными пользователя
     */
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
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(userService.login(loginRequestDto));
    }
}
