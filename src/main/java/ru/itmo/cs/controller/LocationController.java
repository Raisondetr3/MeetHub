package ru.itmo.cs.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.location.LocationDto;
import ru.itmo.cs.entity.Location;
import ru.itmo.cs.service.LocationService;
import ru.itmo.cs.util.EntityMapper;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location API", description = "API для управления местоположениями")
public class LocationController {

    private final LocationService locationService;
    private final EntityMapper entityMapper;

    /**
     * Создаёт новое местоположение.
     *
     * @param locationDto DTO местоположения
     * @return созданное местоположение
     */
    @PostMapping
    @Operation(summary = "Создаёт новое местоположение")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Местоположение успешно создано"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<LocationDto> createLocation(@RequestBody @Valid LocationDto locationDto) {
        Location location = entityMapper.toLocationEntity(locationDto);
        Location createdLocation = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityMapper.toLocationDto(createdLocation));
    }

    /**
     * Возвращает местоположение по его идентификатору.
     *
     * @param id идентификатор местоположения
     * @return местоположение
     */
    @GetMapping("/{id}")
    @Operation(summary = "Возвращает местоположение по его идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Местоположение успешно найдено"),
        @ApiResponse(responseCode = "404", description = "Местоположение не найдено")
    })
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Integer id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(entityMapper.toLocationDto(location));
    }

    /**
     * Возвращает список местоположений по стране.
     *
     * @param country название страны
     * @return список местоположений
     */
    @GetMapping("/country/{country}")
    @Operation(summary = "Возвращает список местоположений по стране")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список местоположений успешно получен")
    })
    public ResponseEntity<List<LocationDto>> getLocationsByCountry(@PathVariable String country) {
        List<Location> locations = locationService.getLocationsByCountry(country);
        List<LocationDto> locationDtos = locations.stream()
            .map(entityMapper::toLocationDto)
            .toList();
        return ResponseEntity.ok(locationDtos);
    }

    /**
     * Возвращает список местоположений по городу.
     *
     * @param city название города
     * @return список местоположений
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Возвращает список местоположений по городу")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список местоположений успешно получен")
    })
    public ResponseEntity<List<LocationDto>> getLocationsByCity(@PathVariable String city) {
        List<Location> locations = locationService.getLocationsByCity(city);
        List<LocationDto> locationDtos = locations.stream()
            .map(entityMapper::toLocationDto)
            .toList();
        return ResponseEntity.ok(locationDtos);
    }

    /**
     * Обновляет данные местоположения.
     *
     * @param id идентификатор местоположения
     * @param locationDto объект DTO местоположения
     * @return обновлённое местоположение
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновляет данные местоположения")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Местоположение успешно обновлено"),
        @ApiResponse(responseCode = "404", description = "Местоположение не найдено"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<LocationDto> updateLocation(@PathVariable Integer id, @RequestBody @Valid LocationDto locationDto) {
        Location locationDetails = entityMapper.toLocationEntity(locationDto);
        Location updatedLocation = locationService.updateLocation(id, locationDetails);
        return ResponseEntity.ok(entityMapper.toLocationDto(updatedLocation));
    }

    /**
     * Удаляет местоположение по его идентификатору.
     *
     * @param id идентификатор местоположения
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удаляет местоположение по его идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Местоположение успешно удалено"),
        @ApiResponse(responseCode = "404", description = "Местоположение не найдено")
    })
    public ResponseEntity<Void> deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
