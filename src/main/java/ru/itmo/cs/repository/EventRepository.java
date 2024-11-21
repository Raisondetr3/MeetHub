package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Event> findByCategory_Name(String categoryName);

    @Query("SELECT e FROM Event e JOIN FETCH e.food WHERE e.id = :eventId")
    Optional<Event> findByIdWithFood(@Param("eventId") Integer eventId);
}

