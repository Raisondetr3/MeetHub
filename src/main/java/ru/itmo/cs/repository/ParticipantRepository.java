package ru.itmo.cs.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Participant;

/**
 * Репозиторий участника для обращения к БД.
 */
@Repository
public interface ParticipantRepository
        extends JpaRepository<Participant, Participant.ParticipantId> {

    /**
     * Находит всех участников мероприятия по ID мероприятия.
     *
     * @param eventId ID мероприятия
     * @return список участников
     */
    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId")
    List<Participant> findByEventId(@Param("eventId") Integer eventId);

    /**
     * Находит всех участников по ID пользователя.
     *
     * @param userId ID пользователя
     * @return список участников
     */
    @Query("SELECT p FROM Participant p WHERE p.user.id = :userId")
    List<Participant> findByUserId(@Param("userId") Integer userId);


    /**
     * Находит участника по мероприятию как создателя.
     *
     * @param event мероприятие, которого создал пользователь
     * @return участник
     */
    @Query("SELECT p FROM Participant p WHERE p.event = :event AND p.isCreator = true")
    Optional<Participant> findEventCreator(@Param("event") Event event);

    /**
     * Находит участника по пользователю.
     *
     * @param id участника, которое ссылается на пользователя
     * @return участник
     */
    boolean existsById(Participant.ParticipantId id);

    /**
     * Проверяет существование участника по ID пользователя и ID мероприятия.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     * @return true, если участник существует
     */
    @Query("""
    SELECT COUNT(p) > 0
    FROM Participant p
    WHERE p.user.id = :userId AND p.event.id = :eventId
    """)
    boolean existsByUserIdAndEventId(@Param("userId") Integer userId, @Param("eventId") Integer eventId);

    /**
     * Проверяет, является ли пользователь организатором мероприятия.
     *
     * @param eventId ID мероприятия
     * @param userId  ID пользователя
     * @return true, если пользователь организатор
     */
    @Query("""
        SELECT COUNT(p) > 0
        FROM Participant p
        WHERE p.event.id = :eventId AND p.user.id = :userId AND p.isCreator = true
        """)
    boolean isOrganizer(@Param("eventId") Integer eventId, @Param("userId") Integer userId);
}
