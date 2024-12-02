package ru.itmo.cs;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Приложение.
 */
@SpringBootApplication
public class MeetHub {
    /**
     * Основной класс.
     *
     * @param args аргументы запуска при необходимости.
     */
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(MeetHub.class, args);
    }
}
