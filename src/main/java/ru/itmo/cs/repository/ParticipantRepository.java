package ru.itmo.cs.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.entity.User;

/**
 * Репозиторий участника для обращения к БД.
 */
@Repository
public interface ParticipantRepository
    extends JpaRepository<Participant, Participant.ParticipantId> {
  /**
   * Находит участника по мероприятию.
   *
   * @param event мероприятие, в котором участвовал пользователь
   *
   * @return участник
   */
  List<Participant> findByEvent(Event event);

  /**
   * Находит участника по пользователю.
   *
   * @param user пользователь, который является участником
   *
   * @return участник
   */
  List<Participant> findByUser(User user);

  /**
   * Находит участника по мероприятию как создателя.
   *
   * @param event мероприятие, которого создал пользователь
   *
   * @return участник
   */
  @Query("SELECT p FROM Participant p WHERE p.event = :event AND p.isCreator = true")
  Optional<Participant> findEventCreator(@Param("event") Event event);
}
