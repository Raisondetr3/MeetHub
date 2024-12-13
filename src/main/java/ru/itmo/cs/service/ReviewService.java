package ru.itmo.cs.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.review.ReviewDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Rating;
import ru.itmo.cs.entity.Review;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.repository.ReviewRepository;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

/**
 * Сервис для работы с отзывами.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EntityMapper entityMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Получает отзывы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список отзывов в виде DTO
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByUser(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        List<Review> reviews = reviewRepository.findByUser(user);
        return reviews.stream()
            .map(entityMapper::toReviewDto)
            .toList();
    }

    /**
     * Получает отзывы мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @return список отзывов в виде DTO
     * @throws ResourceNotFoundException если мероприятие не найдено
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));

        List<Review> reviews = reviewRepository.findByEvent(event);
        return reviews.stream()
            .map(entityMapper::toReviewDto)
            .toList();
    }

    /**
     * Получает отзывы мероприятия с указанным рейтингом.
     *
     * @param eventId идентификатор мероприятия
     * @param rating рейтинг
     * @return список отзывов в виде DTO
     * @throws ResourceNotFoundException если мероприятие не найдено
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByEventAndRating(Integer eventId, Rating rating) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));

        List<Review> reviews = reviewRepository.findByEventAndRating(event, rating);
        return reviews.stream()
            .map(entityMapper::toReviewDto)
            .toList();
    }

    /**
     * Удаляет отзыв по его ID.
     *
     * @param reviewId идентификатор отзыва
     * @param user текущий пользователь
     * @throws ResourceNotFoundException если отзыв не найден
     * @throws AccessDeniedException если пользователь не является автором отзыва
     */
    public void deleteReview(Integer reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Пользователь не имеет права удалять этот отзыв");
        }

        reviewRepository.deleteById(reviewId);
    }

    /**
     * Создает новый отзыв, используя функцию PL/pgSQL.
     *
     * @param reviewDto DTO отзыва
     * @param user текущий пользователь
     * @throws ResourceNotFoundException если пользователь или мероприятие не найдены
     */
    public void createReviewWithFunction(ReviewDto reviewDto, User user) {
        Event event = eventRepository.findById(reviewDto.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));

        String sql = "SELECT leave_review(?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, user.getId(), event.getId(), reviewDto.getRating().name(), reviewDto.getComment());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выполнения PL/pgSQL функции", e);
        }
    }
}