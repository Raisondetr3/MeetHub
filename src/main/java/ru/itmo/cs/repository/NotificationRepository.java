package ru.itmo.cs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Notification;

/**
 * Репозиторий уведомления для работы с БД.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Находит уведомления по ID пользователя.
     *
     * @param userId ID пользователя
     * @return список уведомлений
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId")
    List<Notification> findByUserId(@Param("userId") Integer userId);

    /**
     * Находит уведомления по ID мероприятия.
     *
     * @param eventId ID мероприятия
     * @return список уведомлений
     */
    @Query("SELECT n FROM Notification n WHERE n.event.id = :eventId")
    List<Notification> findByEventId(@Param("eventId") Integer eventId);

    /**
     * Находит последние уведомления пользователя, отсортированные по дате отправки.
     *
     * @param userId ID пользователя
     * @return список уведомлений
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.sentAt DESC")
    List<Notification> findLatestByUserId(@Param("userId") Integer userId);
}