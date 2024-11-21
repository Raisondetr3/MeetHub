package ru.itmo.cs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Location;
import ru.itmo.cs.entity.Venue;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Integer> {
    List<Venue> findByNameContainingIgnoreCase(String name);
    Optional<Venue> findByLocation(Location location);
}

