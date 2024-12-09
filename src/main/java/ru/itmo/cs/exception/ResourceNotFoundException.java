package ru.itmo.cs.exception;

/**
 * Исключение, выбрасываемое, если мероприятие не найдено.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Конструктор исключения с сообщением.
     *
     * @param message сообщение, описывающее причину исключения
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}