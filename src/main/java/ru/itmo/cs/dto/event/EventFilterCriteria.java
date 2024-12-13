package ru.itmo.cs.dto.event;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для фильтрации мероприятий.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для фильтрации мероприятий")
public class EventFilterCriteria {

    @Schema(description = "Название мероприятия", example = "Tech Conference 2024")
    private String name;

    @Schema(description = "Категория мероприятия для фильтрации", example = "Конференция")
    private String category;

    @Schema(description = "Город для фильтрации мероприятий", example = "Москва")
    private String city;

    @Schema(description = "Дата начала фильтрации мероприятий", example = "2024-01-01T00:00:00")
    private LocalDateTime dateFrom;

    @Schema(description = "Дата окончания фильтрации мероприятий", example = "2024-12-31T23:59:59")
    private LocalDateTime dateTo;
}
