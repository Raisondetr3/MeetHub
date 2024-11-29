package ru.itmo.cs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Notification;
import ru.itmo.cs.entity.User;

/**
 * Репозиторий уведомления для обращения к БД.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
  /**
   * Находит уведомление по пользователю.
   * @param user пользователь, к которому отправлено уведомления
   * @return уведомление
   */
  List<Notification> findByUser(User user);

  /**
   * Находит уведомление по мероприятию.
   *
   * @param event мероприятие, с которым связано уведомления
   *
   * @return уведомление
   */
  List<Notification> findByEvent(Event event);

  /**
   * Находит последнее уведомление по пользователю.
   *
   * @param user пользователь, к которому отправлено уведомления
   *
   * @return уведомление
   */
  List<Notification> findByUserOrderBySentAtDesc(User user);
}
