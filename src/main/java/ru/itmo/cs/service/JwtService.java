package ru.itmo.cs.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с JWT токенами.
 */
@Service
@Setter
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Getter
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Генерирует JWT токен для указанного пользователя.
     *
     * @param username имя пользователя
     * @return JWT токен
     */
    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    /**
     * Проверяет, валиден ли токен.
     *
     * @param token токен
     * @param username имя пользователя
     * @return true, если токен валиден
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Извлекает имя пользователя из токена.
     *
     * @param token токен
     * @return имя пользователя
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token токен
     * @return true, если токен истек
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлекает дату истечения срока действия токена.
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает указанное поле из токена.
     *
     * @param token токен
     * @param claimsResolver функция для извлечения поля
     * @param <T> тип поля
     * @return значение поля
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .setAllowedClockSkewSeconds(5)
            .parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }
}
