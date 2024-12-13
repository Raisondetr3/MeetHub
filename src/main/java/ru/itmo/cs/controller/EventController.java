package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.pagination.PaginationResponseDto;
import ru.itmo.cs.dto.participant.ParticipantDto;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.EventService;
import ru.itmo.cs.util.EntityMapper;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "API для управления мероприятиями")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Получение списка мероприятий с фильтрацией и пагинацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список мероприятий успешно получен")
    })
    public ResponseEntity<PaginationResponseDto<EventDto>> getAllEvents(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "date") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {

        Page<EventDto> eventsPage = eventService.getFilteredEvents(
                name, category, city, dateFrom, dateTo, page, size, sortBy, sortDir);

        PaginationResponseDto<EventDto> response = new PaginationResponseDto<>(
                eventsPage.getContent(),
                eventsPage.getNumber(),
                eventsPage.getTotalElements(),
                eventsPage.getTotalPages()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Получение мероприятия по ID.
     *
     * @param eventId идентификатор мероприятия
     * @return DTO мероприятия
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "Получение мероприятия по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Мероприятие успешно найдено"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    })
    public ResponseEntity<EventDto> getEventById(@PathVariable Integer eventId) {
        EventDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Создание нового мероприятия.
     *
     * @param eventDto DTO мероприятия
     * @param user     текущий пользователь
     * @return созданное мероприятие
     */
    @PostMapping
    @Operation(summary = "Создание нового мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Мероприятие успешно создано"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<EventDto> createEvent(
            @RequestBody @Valid EventDto eventDto,
            @AuthenticationPrincipal User user) {
        EventDto createdEvent = eventService.createEvent(eventDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Обновление мероприятия.
     *
     * @param eventId  идентификатор мероприятия
     * @param eventDto DTO мероприятия
     * @param user     текущий пользователь
     * @return обновленное мероприятие
     */
    @PutMapping("/{eventId}")
    @Operation(summary = "Обновление мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Мероприятие успешно обновлено"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    })
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable Integer eventId,
            @RequestBody @Valid EventDto eventDto,
            @AuthenticationPrincipal User user) {
        EventDto updatedEvent = eventService.updateEvent(eventId, eventDto, user);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Удаление мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @param user    текущий пользователь
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Удаление мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Мероприятие успешно удалено"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    })
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal User user) {
        eventService.deleteEvent(eventId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение списка участников мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @param user    текущий пользователь
     * @return список участников
     */
    @GetMapping("/{eventId}/participants")
    @Operation(summary = "Получение списка участников мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список участников успешно получен"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    })
    public ResponseEntity<List<ParticipantDto>> getParticipants(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal User user) {
        List<ParticipantDto> participants = eventService.getParticipants(eventId, user);
        return ResponseEntity.ok(participants);
    }

    /**
     * Регистрация пользователя на мероприятие.
     *
     * @param eventId идентификатор мероприятия
     * @param user    текущий пользователь
     */
    @PostMapping("/{eventId}/register")
    @Operation(summary = "Регистрация пользователя на мероприятие")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
        @ApiResponse(responseCode = "400", description = "Пользователь уже зарегистрирован"),
        @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
})
    public ResponseEntity<Void> registerUserForEvent(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal User user) {
        eventService.registerUserForEvent(eventId, user);
        return ResponseEntity.ok().build();
    }
}
