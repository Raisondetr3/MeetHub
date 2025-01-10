package ru.itmo.cs.dto.participant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления участника мероприятия.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для участника мероприятия")
public class ParticipantDto {

    @Schema(description = "ID пользователя", example = "1")
    private Integer userId;

    @Schema(description = "Имя пользователя", example = "JohnDoe")
    private String username;

    @Schema(description = "Электронная почта пользователя", example = "johndoe@example.com")
    private String email;

    @Schema(description = "Является ли организатором", example = "true")
    private Boolean isCreator;
}
