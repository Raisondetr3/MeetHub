package ru.itmo.cs.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления местоположения.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления местоположения")
public class LocationDto {

    @Schema(description = "Идентификатор местоположения", example = "1")
    private Integer id;

    @NotBlank(message = "Страна не должна быть пустой")
    @Schema(description = "Страна", example = "Россия", required = true)
    private String country;

    @NotBlank(message = "Регион не должен быть пустым")
    @Schema(description = "Регион", example = "Санкт-Петербург", required = true)
    private String region;

    @NotBlank(message = "Город не должен быть пустым")
    @Schema(description = "Город", example = "Санкт-Петербург", required = true)
    private String city;

    @NotBlank(message = "Адрес не должен быть пустым")
    @Schema(description = "Адрес", example = "Невский проспект, 1", required = true)
    private String address;
}
