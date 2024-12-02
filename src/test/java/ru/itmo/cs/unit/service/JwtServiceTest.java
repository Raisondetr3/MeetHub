package ru.itmo.cs.unit.service;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import ru.itmo.cs.service.JwtService;


class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "ItueRrc47BmhZ69S2zB69DcCQiDZF4843IWh1VCzeVw=";
    private final long jwtExpiration = 3600000; // 1 час в миллисекундах

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.setSecretKey(secretKey);
        jwtService.setJwtExpiration(jwtExpiration);
    }

    @Test
    @DisplayName("Успешная генерация и валидация токена")
    void shouldGenerateAndValidateToken() {
        String username = "testUser";

        String token = jwtService.generateToken(username);

        assertNotNull(token, "Токен должен быть сгенерирован");
        assertTrue(jwtService.isTokenValid(token, username), "Токен должен быть валидным");
        assertEquals(username, jwtService.extractUsername(token), "Имя пользователя должно быть извлечено корректно");
    }

    @Test
    @DisplayName("Токен не валиден для другого имени пользователя")
    void shouldInvalidateTokenWithWrongUsername() {
        String username = "testUser";
        String token = jwtService.generateToken(username);

        assertFalse(jwtService.isTokenValid(token, "otherUser"), "Токен не должен быть валидным для другого пользователя");
    }

    @Test
    @DisplayName("Обнаружение истекшего токена")
    void shouldDetectExpiredToken() {
        String username = "testUser";
        jwtService.setJwtExpiration(-2000);
        String token = jwtService.generateToken(username);

        boolean isValid = jwtService.isTokenValid(token, username);

        assertFalse(isValid, "Токен с истекшим сроком действия не должен быть валидным");
    }

    @Test
    @DisplayName("Ошибка при обработке некорректного токена")
    void shouldThrowExceptionForMalformedToken() {
        String malformedToken = "invalid.token";

        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(malformedToken),
            "Должно быть выброшено исключение для некорректного токена");
    }

    @Test
    @DisplayName("Успешное извлечение имени пользователя из токена")
    void shouldExtractUsernameFromToken() {
        String username = "testUser";
        String token = jwtService.generateToken(username);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(username, extractedUsername, "Имя пользователя должно быть извлечено корректно");
    }
}
