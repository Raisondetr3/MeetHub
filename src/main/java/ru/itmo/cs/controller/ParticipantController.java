package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.participant.ParticipantDto;
import ru.itmo.cs.service.facade.ParticipantFacade;

import java.util.List;

@RestController
@RequestMapping("/api/v1/participants")
@RequiredArgsConstructor
@Tag(name = "Participant API", description = "API для управления участниками мероприятий")
public class ParticipantController {

    private final ParticipantFacade participantFacade;

    /**
     * Регистрирует участника в мероприятии.
     *
     * @param eventId        ID мероприятия
     * @param participantDto DTO участника
     * @return зарегистрированный участник
     */
    @PostMapping("/{eventId}")
    @Operation(summary = "Регистрирует участника в мероприятии")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Участник успешно зарегистрирован"),
        @ApiResponse(responseCode = "404", description = "Мероприятие или пользователь не найдены"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<ParticipantDto> registerParticipant(
        @PathVariable Integer eventId,
        @RequestBody @Valid ParticipantDto participantDto) {
        ParticipantDto registeredParticipant = participantFacade.registerParticipant(participantDto, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredParticipant);
    }

    /**
     * Регистрирует организатора для мероприятия.
     *
     * @param eventId ID мероприятия
     * @param userId  ID пользователя
     * @return зарегистрированный организатор
     */
    @PostMapping("/{eventId}/organizer/{userId}")
    @Operation(summary = "Регистрирует организатора для мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Организатор успешно зарегистрирован"),
        @ApiResponse(responseCode = "404", description = "Мероприятие или пользователь не найдены"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<ParticipantDto> registerOrganizer(
        @PathVariable Integer eventId,
        @PathVariable Integer userId) {
        ParticipantDto organizer = participantFacade.registerOrganizer(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(organizer);
    }

    /**
     * Получает список участников для мероприятия.
     *
     * @param eventId ID мероприятия
     * @return список участников
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "Получает список участников для мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список участников успешно получен"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    })
    public ResponseEntity<List<ParticipantDto>> getParticipantsByEvent(@PathVariable Integer eventId) {
        List<ParticipantDto> participants = participantFacade.getParticipantsByEvent(eventId);
        return ResponseEntity.ok(participants);
    }

    /**
     * Удаляет участника из мероприятия.
     *
     * @param eventId ID мероприятия
     * @param userId  ID пользователя
     */
    @DeleteMapping("/{eventId}/user/{userId}")
    @Operation(summary = "Удаляет участника из мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Участник успешно удален"),
        @ApiResponse(responseCode = "404", description = "Участник не найден")
    })
    public ResponseEntity<Void> removeParticipant(
        @PathVariable Integer eventId,
        @PathVariable Integer userId) {
        participantFacade.removeParticipant(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}