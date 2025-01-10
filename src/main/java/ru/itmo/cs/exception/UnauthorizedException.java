package ru.itmo.cs.exception;

/**
 * Исключение для неавторизованных пользователей.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
