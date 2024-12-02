package ru.itmo.cs.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Location;
import ru.itmo.cs.entity.Venue;

/**
 * Репозиторий места проведения для обращения к БД.
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Integer> {
    /**
     * Находит место проведения по названию без учёта регистра.
     *
     * @param name название места проведения
     * @return место проведения
     */
    List<Venue> findByNameContainingIgnoreCase(String name);

    /**
     * Находит место проведения по местоположению.
     *
     * @param location местоположение места проведения
     * @return место проведения
     */
    Optional<Venue> findByLocation(Location location);
}
