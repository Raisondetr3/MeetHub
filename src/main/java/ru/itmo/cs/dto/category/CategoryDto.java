package ru.itmo.cs.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления категории.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления категории мероприятия")
public class CategoryDto {

    @Schema(description = "Идентификатор категории", example = "1")
    private Integer id;

    @Schema(description = "Название категории", example = "Конференция")
    private String name;
}

