package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.review.ReviewDto;
import ru.itmo.cs.entity.Rating;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.ReviewService;
import ru.itmo.cs.service.UserService;

/**
 * Контроллер для управления отзывами.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API для управления отзывами")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    /**
     * Получает все отзывы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список отзывов в виде DTO
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить отзывы пользователя", description = "Возвращает все отзывы, оставленные пользователем")
    @ApiResponse(responseCode = "200", description = "Отзывы успешно получены")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Integer userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Получает все отзывы мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @return список отзывов в виде DTO
     */
    @GetMapping("/event/{eventId}")
    @Operation(summary = "Получить отзывы мероприятия", description = "Возвращает все отзывы для указанного мероприятия")
    @ApiResponse(responseCode = "200", description = "Отзывы успешно получены")
    @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    public ResponseEntity<List<ReviewDto>> getReviewsByEvent(@PathVariable Integer eventId) {
        List<ReviewDto> reviews = reviewService.getReviewsByEvent(eventId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Получает отзывы мероприятия с указанным рейтингом.
     *
     * @param eventId идентификатор мероприятия
     * @param rating рейтинг
     * @return список отзывов в виде DTO
     */
    @GetMapping("/event/{eventId}/rating/{rating}")
    @Operation(summary = "Получить отзывы мероприятия с рейтингом", description = "Возвращает отзывы для мероприятия с указанным рейтингом")
    @ApiResponse(responseCode = "200", description = "Отзывы успешно получены")
    @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    public ResponseEntity<List<ReviewDto>> getReviewsByEventAndRating(
        @PathVariable Integer eventId,
        @PathVariable Rating rating
    ) {
        List<ReviewDto> reviews = reviewService.getReviewsByEventAndRating(eventId, rating);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Удаляет отзыв по его ID.
     *
     * @param reviewId идентификатор отзыва
     * @param principal текущий аутентифицированный пользователь
     */
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв текущего пользователя")
    @ApiResponse(responseCode = "204", description = "Отзыв успешно удален")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    public ResponseEntity<Void> deleteReview(
        @PathVariable Integer reviewId,
        Principal principal
    ) {
        User currentUser = userService.me();
        reviewService.deleteReview(reviewId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Создает новый отзыв с использованием PL/pgSQL функции.
     *
     * @param reviewDto DTO отзыва
     * @param principal текущий аутентифицированный пользователь
     */
    @PostMapping("/plpgsql")
    @Operation(summary = "Создать отзыв с PL/pgSQL", description = "Создает новый отзыв с использованием PL/pgSQL функции")
    @ApiResponse(responseCode = "201", description = "Отзыв успешно создан")
    @ApiResponse(responseCode = "404", description = "Мероприятие не найдено")
    public ResponseEntity<Void> createReviewWithFunction(
        @Valid @RequestBody ReviewDto reviewDto,
        Principal principal
    ) {
        User currentUser = userService.me();
        reviewService.createReviewWithFunction(reviewDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
