package ru.itmo.cs.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.User;

/**
 * Репозиторий пользователя для обращения к БД.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  /**
   * Находит пользователя по имени.
   *
   * @param username имя пользователя
   *
   * @return пользователь
   */
  Optional<User> findByUsername(String username);

  /**
   * Находит пользователя по причастному мероприятию.
   *
   * @param eventId ID мероприятия
   *
   * @return пользователь
   */
  @Query("SELECT u FROM User u JOIN Participant p ON u.id = p.id.userId WHERE p.event.id = :eventId")
  List<User> findParticipantsByEventId(@Param("eventId") Integer eventId);
}

