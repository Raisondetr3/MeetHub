package ru.itmo.cs.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления билета.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления билета")
public class TicketDto {

    @Schema(description = "Идентификатор билета", example = "1")
    private Integer id;

    @Schema(description = "Номер места", example = "A10")
    private String seatNumber;

    @Schema(description = "Идентификатор мероприятия", example = "1")
    private Integer eventId;

    @Schema(description = "Идентификатор пользователя", example = "1")
    private Integer userId;
}

