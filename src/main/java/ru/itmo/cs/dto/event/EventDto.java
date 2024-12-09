package ru.itmo.cs.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.dto.category.CategoryDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для представления мероприятия.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления мероприятия")
public class EventDto {

    @Schema(description = "Идентификатор мероприятия", example = "1")
    private Integer id;

    @Schema(description = "Название мероприятия", example = "Tech Conference 2024")
    private String name;

    @Schema(description = "Описание мероприятия", example = "Конференция о новых технологиях.")
    private String description;

    @Schema(description = "Дата и время проведения мероприятия", example = "2024-12-31T18:00:00")
    private LocalDateTime date;

    @Schema(description = "Место проведения мероприятия")
    private VenueDto venue;

    @Schema(description = "Категория мероприятия")
    private CategoryDto category;

    @Schema(description = "Список доступной еды на мероприятии")
    private List<FoodDto> food;
}


