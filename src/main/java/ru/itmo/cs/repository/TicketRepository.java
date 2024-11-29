package ru.itmo.cs.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Ticket;
import ru.itmo.cs.entity.User;

/**
 * Репозиторий билета для обращения к БД.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
  /**
   * Находит билета по пользователю.
   *
   * @param user пользователь, который приобрёл билет
   *
   * @return билет
   */
  List<Ticket> findByUser(User user);

  /**
   * Находит билета по мероприятию.
   *
   * @param event мероприятие, где используется билет
   *
   * @return билет
   */
  List<Ticket> findByEvent(Event event);

  /**
   * Находит билета по номеру места и мероприятию.
   *
   * @param seatNumber номер места
   *
   * @param event мероприятие, где используется билет
   *
   * @return билет
   */
  Optional<Ticket> findBySeatNumberAndEvent(String seatNumber, Event event);
}
