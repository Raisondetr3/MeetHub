package ru.itmo.cs.dto.notification;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления уведомления.
 */
@Schema(description = "DTO для представления уведомлений")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    @Schema(description = "Идентификатор уведомления", example = "1")
    private Integer id;

    @Schema(description = "ID пользователя, получателя уведомления", example = "1")
    private Integer userId;

    @Schema(description = "Имя пользователя", example = "JohnDoe")
    private String username;

    @Schema(description = "Электронная почта пользователя", example = "johndoe@example.com")
    private String userEmail;

    @Schema(description = "ID мероприятия, связанного с уведомлением", example = "1")
    private Integer eventId;

    @Schema(description = "Название мероприятия", example = "Tech Conference 2024")
    private String eventName;

    @Schema(description = "Содержимое уведомления", example = "Напоминание о мероприятии")
    private String content;

    @Schema(description = "Статус уведомления", example = "SENT")
    private String status;

    @Schema(description = "Время отправки уведомления", example = "2024-12-05T10:15:30")
    private Date sentAt;
}


