package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Ticket;
import ru.itmo.cs.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByUser(User user);

    List<Ticket> findByEvent(Event event);

    Optional<Ticket> findBySeatNumberAndEvent(String seatNumber, Event event);
}

