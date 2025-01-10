package ru.itmo.cs.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.entity.Rating;

import java.util.Date;

/**
 * DTO для представления отзыва.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления отзыва")
public class ReviewDto {

    @Schema(description = "Идентификатор отзыва", example = "1")
    private Integer id;

    @Schema(description = "Идентификатор пользователя", example = "123")
    private Integer userId;

    @Schema(description = "Имя пользователя", example = "JohnDoe")
    private String username;

    @Schema(description = "Идентификатор мероприятия", example = "456")
    private Integer eventId;

    @Schema(description = "Название мероприятия", example = "Tech Conference")
    private String eventName;

    @Schema(description = "Рейтинг", example = "FIVE_STARS")
    private Rating rating;

    @Schema(description = "Комментарий", example = "Отличное мероприятие!")
    private String comment;

    @Schema(description = "Дата создания", example = "2023-12-10T15:30:00")
    private Date createdAt;
}
