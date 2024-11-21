package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Notification;
import ru.itmo.cs.entity.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser(User user);

    List<Notification> findByEvent(Event event);

    List<Notification> findByUserOrderBySentAtDesc(User user);
}

