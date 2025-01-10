package ru.itmo.cs.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
     * Обрабатывает исключение AuthenticationException.
     *
     * @param ex исключение, выбрасываемое при ошибке аутентификации
     * @return ResponseEntity с сообщением об ошибке и статусом 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Map.of(
                "error", "Unauthorized",
                "message", "Неверное имя пользователя или пароль"
            )
        );
    }

    /**
     * Обрабатывает исключение ResourceNotFoundException.
     *
     * @param ex исключение, выбрасываемое при попытке найти несуществующий ресурс
     * @return ResponseEntity с сообщением об ошибке и статусом 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleEventNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Map.of(
                "error", "Not Found",
                "message", ex.getMessage()
            )
        );
    }

    /**
     * Обрабатывает ValidationException.
     *
     * @param ex исключение валидации
     * @return ResponseEntity с сообщением об ошибке и статусом 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Validation Error",
                "message", ex.getMessage()
            )
        );
    }

    /**
     * Обрабатывает UnauthorizedException.
     *
     * @param ex исключение авторизации
     * @return ResponseEntity с сообщением об ошибке и статусом 401 Unauthorized
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Map.of(
                "error", "Unauthorized",
                "message", ex.getMessage()
            )
        );
    }

    /**
     * Обрабатывает IllegalArgumentException.
     *
     * @param ex исключение, выбрасываемое при неправильных аргументах
     * @return ResponseEntity с сообщением об ошибке и статусом 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Invalid Argument",
                "message", ex.getMessage()
            )
        );
    }

    /**
     * Обрабатывает исключения валидации, возникающие при некорректных данных в запросе.
     *
     * @param ex исключение MethodArgumentNotValidException, содержащее детали ошибок валидации
     * @return карта с ошибками, где ключ — имя поля, а значение — сообщение об ошибке
     * @see MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return errors;
    }
}
