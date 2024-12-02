package ru.itmo.cs.exception;

/**
 * Исключение, выбрасываемое, если пользователь уже существует.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
