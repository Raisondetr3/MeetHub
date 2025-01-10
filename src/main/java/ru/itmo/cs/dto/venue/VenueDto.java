package ru.itmo.cs.dto.venue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.itmo.cs.dto.location.LocationDto;

/**
 * DTO для представления места проведения мероприятий.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления места проведения мероприятий")
public class VenueDto {

    @Schema(description = "Идентификатор места проведения", example = "1")
    private Integer id;

    @Schema(description = "Название места проведения", example = "Главный зал")
    private String name;

    @Schema(description = "Вместимость зала", example = "500")
    private Integer capacity;

    @Schema(description = "Местоположение", example = "Москва, ул. Ленина, 12")
    private LocationDto location;
}
