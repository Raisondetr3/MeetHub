package ru.itmo.cs.exception;

/**
 * Исключение для ошибок валидации.
 */
public class ValidationException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public ValidationException(String message) {
        super(message);
    }
}
