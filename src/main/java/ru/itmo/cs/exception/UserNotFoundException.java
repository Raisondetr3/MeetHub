package ru.itmo.cs.exception;

/**
 * Исключение, выбрасываемое, если пользователь не найден.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
