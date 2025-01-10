package ru.itmo.cs.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * @return мероприятие
     */
    Event findByName(String name);

    /**
     * Находит мероприятие по диапазону дат.
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return мероприятие
     */
    List<Event> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Находит мероприятие по названию категории.
     *
     * @param categoryName название категории
     * @return мероприятие
     */
    List<Event> findByCategoryName(CategoryEnum categoryName);

    /**
     * Находит мероприятие по ID c едой.
     *
     * @param eventId ID мероприятия
     * @return мероприятие
     */
    @Query("SELECT e FROM Event e JOIN FETCH e.food WHERE e.id = :eventId")
    Optional<Event> findByIdWithFood(@Param("eventId") Integer eventId);

    /**
     * Находит мероприятия по заданным фильтрам.
     *
     * @param name       название мероприятия (частичное совпадение)
     * @param category   название категории
     * @param city       название города
     * @param dateFrom   дата начала
     * @param dateTo     дата окончания
     * @param pageable   объект для пагинации и сортировки
     * @return страница мероприятий
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:category IS NULL OR e.category.name = :category)
          AND (:city IS NULL OR LOWER(e.venue.location.city) = LOWER(:city))
          AND (:dateFrom IS NULL OR e.date >= :dateFrom)
          AND (:dateTo IS NULL OR e.date <= :dateTo)
        """)
    Page<Event> findByFilters(@Param("name") String name,
                              @Param("category") String category,
                              @Param("city") String city,
                              @Param("dateFrom") LocalDateTime dateFrom,
                              @Param("dateTo") LocalDateTime dateTo,
                              Pageable pageable);
}
