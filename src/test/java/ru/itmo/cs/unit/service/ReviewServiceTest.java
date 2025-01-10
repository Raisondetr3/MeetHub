package ru.itmo.cs.unit.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import ru.itmo.cs.dto.review.ReviewDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Rating;
import ru.itmo.cs.entity.Review;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.repository.ReviewRepository;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.service.ReviewService;
import ru.itmo.cs.util.EntityMapper;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private User user;
    private Event event;
    private Review review;
    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        user = new User(1, "testUser", "test@example.com", "password");
        event = new Event(1, "Test Event", "Test Description", LocalDateTime.now(), null, null, List.of(), LocalDateTime.now());
        review = new Review(1, user, event, Rating.FIVE_STARS, "Excellent!", new Date());
        reviewDto = new ReviewDto(
            1,
            user.getId(),
            user.getUsername(),
            event.getId(),
            event.getName(),
            Rating.FIVE_STARS,
            "Excellent!",
            new Date()
        );
    }

    @Test
    @DisplayName("Ошибка при создании отзыва для несуществующего мероприятия")
    void shouldThrowExceptionIfEventNotFound() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> reviewService.createReviewWithFunction(reviewDto, user),
            "Ожидалось исключение, так как мероприятие не найдено"
        );

        assertEquals("Мероприятие не найдено", exception.getMessage());
        verify(eventRepository).findById(event.getId());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Успешное получение отзывов пользователя")
    void shouldGetReviewsByUserSuccessfully() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reviewRepository.findByUser(user)).thenReturn(List.of(review));
        when(entityMapper.toReviewDto(review)).thenReturn(reviewDto);

        // Act
        List<ReviewDto> result = reviewService.getReviewsByUser(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reviewDto, result.get(0));
        verify(userRepository).findById(user.getId());
        verify(reviewRepository).findByUser(user);
    }

    @Test
    @DisplayName("Ошибка при получении отзывов для несуществующего пользователя")
    void shouldThrowExceptionIfUserNotFound() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> reviewService.getReviewsByUser(user.getId()),
            "Ожидалось исключение, так как пользователь не найден"
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository).findById(user.getId());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Успешное удаление отзыва")
    void shouldDeleteReviewSuccessfully() {
        // Arrange
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        // Act
        reviewService.deleteReview(review.getId(), user);

        // Assert
        verify(reviewRepository).findById(review.getId());
        verify(reviewRepository).deleteById(review.getId());
    }

    @Test
    @DisplayName("Ошибка при удалении чужого отзыва")
    void shouldThrowExceptionWhenDeletingAnotherUsersReview() {
        // Arrange
        User anotherUser = new User(2, "anotherUser", "another@example.com", "password");
        review.setUser(anotherUser);
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> reviewService.deleteReview(review.getId(), user),
            "Ожидалось исключение при попытке удаления чужого отзыва"
        );

        assertEquals("Пользователь не имеет права удалять этот отзыв", exception.getMessage());
        verify(reviewRepository).findById(review.getId());
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего отзыва")
    void shouldThrowExceptionWhenDeletingNonExistentReview() {
        // Arrange
        Integer nonExistentReviewId = 999;
        when(reviewRepository.findById(nonExistentReviewId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> reviewService.deleteReview(nonExistentReviewId, user)
        );

        assertEquals("Отзыв не найден", exception.getMessage());

        // Verify no further interactions happen
        verify(reviewRepository).findById(nonExistentReviewId);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Успешное создание отзыва через функцию PL/pgSQL")
    void shouldCreateReviewWithFunctionSuccessfully() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        // Act
        reviewService.createReviewWithFunction(reviewDto, user);

        // Assert
        String sql = "SELECT leave_review(?, ?, ?, ?)";
        verify(jdbcTemplate).update(sql, user.getId(), event.getId(), reviewDto.getRating().name(), reviewDto.getComment());
    }

    @Test
    @DisplayName("Ошибка при создании отзыва через функцию PL/pgSQL для несуществующего мероприятия")
    void shouldThrowExceptionIfEventNotFoundUsingFunction() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> reviewService.createReviewWithFunction(reviewDto, user),
            "Ожидалось исключение, так как мероприятие не найдено"
        );

        assertEquals("Мероприятие не найдено", exception.getMessage());
        verifyNoInteractions(jdbcTemplate);
    }
}
