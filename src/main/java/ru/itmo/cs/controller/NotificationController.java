package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.notification.NotificationDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.facade.NotificationFacade;
import ru.itmo.cs.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "API для управления уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationFacade notificationFacade;

    /**
     * Создаёт новое уведомление.
     *
     * @param eventId идентификатор мероприятия
     * @param notificationDto DTO уведомления
     * @return созданное уведомление
     */
    @Operation(summary = "Создать уведомление для мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Уведомление успешно создано",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NotificationDto.class))),
        @ApiResponse(responseCode = "404", description = "Мероприятие или организатор не найдены",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{eventId}")
    public ResponseEntity<NotificationDto> createNotification(
        @PathVariable Integer eventId,
        @RequestBody @Valid NotificationDto notificationDto) {
        User organizer = notificationFacade.getEventOrganizer(eventId);
        Event event = notificationFacade.getEventEntityById(eventId);

        NotificationDto createdNotification = notificationService.createNotification(
            notificationDto,
            organizer,
            event
        );

        return ResponseEntity.status(201).body(createdNotification);
    }

    /**
     * Отправляет напоминания участникам мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @return ответ об успешной отправке напоминаний
     */
    @Operation(summary = "Отправить напоминания участникам")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Напоминания успешно отправлены"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400",
            description = "Напоминание может быть отправлено только за 24 часа до мероприятия",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{eventId}/reminders")
    public ResponseEntity<Void> sendReminders(@PathVariable Integer eventId) {
        Event event = notificationFacade.getEventEntityById(eventId);
        notificationService.sendReminderToParticipants(event);
        return ResponseEntity.ok().build();
    }

    /**
     * Получает все уведомления для пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список уведомлений
     */
    @Operation(summary = "Получить уведомления для пользователя")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список уведомлений успешно получен",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NotificationDto.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByUser(@PathVariable Integer userId) {
        List<NotificationDto> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }
}