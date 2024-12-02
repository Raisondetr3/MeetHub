package ru.itmo.cs.exception;

import java.util.Arrays;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Глобальный обработчик исключений для приложения.
 * Предоставляет централизованную обработку исключений для всех контроллеров,
 * обеспечивая унифицированные ответы в случае ошибок.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает все остальные исключения.
     *
     * @param ex любое необработанное исключение
     * @return ResponseEntity с сообщением об ошибке и статусом 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of(
                "error", ex.getMessage(),
                "stackTrace", Arrays.toString(ex.getStackTrace())
            )
        );
    }

    /**
     * Обрабатывает исключение UserAlreadyExistsException.
     *
     * @param ex исключение, выбрасываемое при попытке регистрации
     *           пользователя с уже существующим именем
     * @return ResponseEntity с сообщением об ошибке и статусом 409 Conflict
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of(
                "error", "Conflict",
                "message", ex.getMessage()
            )
        );
    }

    /**
     * Обрабатывает исключение UserNotFoundException.
     *
     * @param ex исключение, выбрасываемое при попытке найти несуществующего пользователя
     * @return ResponseEntity с сообщением об ошибке и статусом 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Map.of(
                "error", "Not Found",
                "message", ex.getMessage()
            )
        );
    }

    /**
     * Обрабатывает исключение AuthenticationException.
     *
     * @param ex исключение, выбрасываемое при ошибке аутентификации
     * @return ResponseEntity с сообщением об ошибке и статусом 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        // 401
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Map.of(
                "error", "Unauthorized",
                "message", "Неверное имя пользователя или пароль"
            )
        );
    }
}
