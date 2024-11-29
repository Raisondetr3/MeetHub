package ru.itmo.cs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Rating;
import ru.itmo.cs.entity.Review;
import ru.itmo.cs.entity.User;

/**
 * Репозиторий отзыва для обращения к БД.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

  /**
   * Находит категорию по пользователю.
   *
   * @param user пользователь, который оставил отзыв
   *
   * @return отзыв
   */
  List<Review> findByUser(User user);

  /**
   * Находит категорию по мероприятию.
   *
   * @param event мероприятие, на котором был оставлен отзыв
   *
   * @return отзыв
   */
  List<Review> findByEvent(Event event);

  /**
   * Находит категорию по мероприятию и рейтинге.
   * @param event мероприятие, на котором был оставлен отзыв
   * @param rating рейтинг
   * @return отзыв
   */
  List<Review> findByEventAndRating(Event event, Rating rating);
}
