package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Находит пользователя по логину.
     *
     * @param username логин пользователя.
     * @return найденный пользователь.
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по электронной почте.
     *
     * @param email электронная почта пользователя.
     * @return найденный пользователь.
     */
    Optional<User> findByEmail(String email);

    /**
     * Находит пользователей, которые участвуют в указанном мероприятии.
     *
     * @param eventId ID мероприятия.
     * @return список пользователей.
     */
    @Query("SELECT u FROM User u JOIN Participant p ON u.id = p.id.userId WHERE p.event.id = :eventId")
    List<User> findParticipantsByEventId(@Param("eventId") Integer eventId);
}
