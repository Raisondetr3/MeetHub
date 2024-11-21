package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Participant.ParticipantId> {
    List<Participant> findByEvent(Event event);

    List<Participant> findByUser(User user);

    @Query("SELECT p FROM Participant p WHERE p.event = :event AND p.isCreator = true")
    Optional<Participant> findEventCreator(@Param("event") Event event);
}

