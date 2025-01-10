package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.service.facade.VenueFacade;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
@Tag(name = "Venue API", description = "API для управления местами проведения мероприятий")
public class VenueController {

    private final VenueFacade venueFacade;

    /**
     * Создаёт новое место проведения мероприятия.
     *
     * @param venueDto DTO места проведения
     * @return созданное место проведения
     */
    @PostMapping
    @Operation(summary = "Создаёт новое место проведения мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Место проведения успешно создано"),
        @ApiResponse(responseCode = "404", description = "Местоположение не найдено"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<VenueDto> createVenue(@RequestBody @Valid VenueDto venueDto) {
        VenueDto createdVenue = venueFacade.createVenue(venueDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenue);
    }

    /**
     * Возвращает место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     * @return место проведения
     */
    @GetMapping("/{id}")
    @Operation(summary = "Возвращает место проведения по его идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Место проведения успешно найдено"),
        @ApiResponse(responseCode = "404", description = "Место проведения не найдено")
    })
    public ResponseEntity<VenueDto> getVenueById(@PathVariable Integer id) {
        VenueDto venue = venueFacade.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    /**
     * Обновляет место проведения мероприятия.
     *
     * @param id идентификатор места проведения
     * @param venueDto объект DTO места проведения
     * @return обновлённое место проведения
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновляет место проведения мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Место проведения успешно обновлено"),
        @ApiResponse(responseCode = "404", description = "Место проведения или местоположение не найдены"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<VenueDto> updateVenue(@PathVariable Integer id, @RequestBody @Valid VenueDto venueDto) {
        VenueDto updatedVenue = venueFacade.updateVenue(id, venueDto);
        return ResponseEntity.ok(updatedVenue);
    }

    /**
     * Удаляет место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удаляет место проведения по его идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Место проведения успешно удалено"),
        @ApiResponse(responseCode = "404", description = "Место проведения не найдено")
    })
    public ResponseEntity<Void> deleteVenue(@PathVariable Integer id) {
        venueFacade.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}