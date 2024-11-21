package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Rating;
import ru.itmo.cs.entity.Review;
import ru.itmo.cs.entity.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByUser(User user);

    List<Review> findByEvent(Event event);

    List<Review> findByEventAndRating(Event event, Rating rating);
}

