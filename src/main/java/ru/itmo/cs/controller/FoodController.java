package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.entity.Food;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.FoodService;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/food")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "API для управления едой мероприятий")
public class FoodController {

    private final FoodService foodService;
    private final EntityMapper entityMapper;

    /**
     * Создает список еды для мероприятия.
     *
     * @param foodDtos список DTO еды
     * @param user текущий пользователь
     * @param eventId идентификатор мероприятия
     * @return список созданной еды
     */
    @PostMapping("/{eventId}")
    @Operation(summary = "Создает список еды для мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Еда успешно создана"),
        @ApiResponse(responseCode = "403", description = "Пользователь не является организатором"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<List<FoodDto>> createFoodList(
        @RequestBody @Valid List<FoodDto> foodDtos,
        @AuthenticationPrincipal User user,
        @PathVariable Integer eventId) {
        List<Food> createdFood = foodService.createFoodList(foodDtos, user, eventId);
        List<FoodDto> foodDtosResponse = createdFood.stream()
            .map(entityMapper::toFoodDto)
            .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(foodDtosResponse);
    }

    /**
     * Создает одно блюдо для мероприятия.
     *
     * @param foodDto DTO блюда
     * @param user текущий пользователь
     * @param eventId идентификатор мероприятия
     * @return созданное блюдо
     */
    @PostMapping("/{eventId}/single")
    @Operation(summary = "Создает одно блюдо для мероприятия")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Блюдо успешно создано"),
        @ApiResponse(responseCode = "403", description = "Пользователь не является организатором"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<FoodDto> createFood(
        @RequestBody @Valid FoodDto foodDto,
        @AuthenticationPrincipal User user,
        @PathVariable Integer eventId) {
        FoodDto createdFood = foodService.createFood(foodDto, user, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFood);
    }

    /**
     * Возвращает список всех блюд.
     *
     * @return список всех блюд
     */
    @GetMapping
    @Operation(summary = "Возвращает список всех блюд")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список успешно получен")
    })
    public ResponseEntity<List<FoodDto>> getAllFood() {
        List<FoodDto> allFood = foodService.getAllFood();
        return ResponseEntity.ok(allFood);
    }

    /**
     * Возвращает блюдо по его ID.
     *
     * @param id идентификатор блюда
     * @return DTO блюда
     */
    @GetMapping("/{id}")
    @Operation(summary = "Возвращает блюдо по его ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Блюдо успешно найдено"),
        @ApiResponse(responseCode = "404", description = "Блюдо не найдено")
    })
    public ResponseEntity<FoodDto> getFoodById(@PathVariable Integer id) {
        Food food = foodService.getFoodById(id);
        return ResponseEntity.ok(entityMapper.toFoodDto(food));
    }

    /**
     * Обновляет данные блюда.
     *
     * @param id идентификатор блюда
     * @param foodDto DTO блюда
     * @param user текущий пользователь
     * @param eventId идентификатор мероприятия
     * @return обновленное блюдо
     */
    @PutMapping("/{eventId}/{id}")
    @Operation(summary = "Обновляет данные блюда")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Блюдо успешно обновлено"),
        @ApiResponse(responseCode = "403", description = "Пользователь не является организатором"),
        @ApiResponse(responseCode = "404", description = "Блюдо не найдено"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<FoodDto> updateFood(
        @PathVariable Integer id,
        @RequestBody @Valid FoodDto foodDto,
        @AuthenticationPrincipal User user,
        @PathVariable Integer eventId) {
        FoodDto updatedFood = foodService.updateFood(id, foodDto, user, eventId);
        return ResponseEntity.ok(updatedFood);
    }

    /**
     * Удаляет блюдо по его ID.
     *
     * @param id идентификатор блюда
     * @param user текущий пользователь
     * @param eventId идентификатор мероприятия
     */
    @DeleteMapping("/{eventId}/{id}")
    @Operation(summary = "Удаляет блюдо по его ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Блюдо успешно удалено"),
        @ApiResponse(responseCode = "403", description = "Пользователь не является организатором"),
        @ApiResponse(responseCode = "404", description = "Блюдо не найдено")
    })
    public ResponseEntity<Void> deleteFood(
        @PathVariable Integer id,
        @AuthenticationPrincipal User user,
        @PathVariable Integer eventId) {
        foodService.deleteFood(id, user, eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ищет блюда по названию.
     *
     * @param name часть названия блюда
     * @return список найденных блюд
     */
    @GetMapping("/search")
    @Operation(summary = "Ищет блюда по названию")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены")
    })
    public ResponseEntity<List<FoodDto>> searchFoodByName(@RequestParam String name) {
        List<FoodDto> searchResults = foodService.searchFoodByName(name);
        return ResponseEntity.ok(searchResults);
    }
}
