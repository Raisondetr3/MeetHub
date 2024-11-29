package ru.itmo.cs.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.CategoryEnum;
import ru.itmo.cs.entity.Event;

/**
 * Репозиторий мероприятия для обращения к БД.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
  /**
   * Находит мероприятие по его названию.
   * Позже изменится на поиск по частичному name (для учета фильтрации)
   *
   * @param name название
   *
   * @return мероприятие
   */
  Event findByName(String name);

  /**
   * Находит мероприятие по диапазону дат.
   *
   * @param startDate начальная дата
   *
   * @param endDate конечная дата
   *
   * @return мероприятие
   */
  List<Event> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Находит мероприятие по названию категории.
   *
   * @param categoryName название категории
   *
   * @return мероприятие
   */
  List<Event> findByCategoryName(CategoryEnum categoryName);

  /**
   * Находит мероприятие по ID c едой.
   *
   * @param eventId ID мероприятия
   *
   * @return мероприятие
   */
  @Query("SELECT e FROM Event e JOIN FETCH e.food WHERE e.id = :eventId")
  Optional<Event> findByIdWithFood(@Param("eventId") Integer eventId);
}
