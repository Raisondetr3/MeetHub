package ru.itmo.cs.dto.food;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления еды.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления еды")
public class FoodDto {

    @Schema(description = "Идентификатор еды", example = "1")
    private Integer id;

    @Schema(description = "Название блюда", example = "Пицца")
    private String name;

    @Schema(description = "Состав блюда", example = "Мука, томаты, сыр")
    private String composition;
}
