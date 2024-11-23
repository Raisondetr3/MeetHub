package ru.itmo.cs.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.entity.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itmo.cs.entity.Venue;
import ru.itmo.cs.repository.LocationRepository;
import ru.itmo.cs.repository.VenueRepository;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@Rollback
class VenueRepositoryTest extends IntegrationTestBase {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private LocationRepository locationRepository;

    private Venue defaultVenue;

    @BeforeEach
    void setUp() {
        Location location = new Location();
        location.setCountry("Russia");
        location.setRegion("Saint Petersburg");
        location.setCity("SPB");
        location.setAddress("Nevsky Prospect, 1");
        Location savedLocation = locationRepository.save(location);

        defaultVenue = new Venue();
        defaultVenue.setName("Main Hall");
        defaultVenue.setLocation(savedLocation);
        defaultVenue.setCapacity(500);
    }

    @Test
    @DisplayName("Should save Venue correctly")
    void testSaveVenue() {
        Venue savedVenue = venueRepository.save(defaultVenue);

        assertThat(savedVenue).isNotNull();
        assertThat(savedVenue.getId()).isNotNull();
        assertThat(savedVenue.getName()).isEqualTo("Main Hall");
        assertThat(savedVenue.getCapacity()).isEqualTo(500);
        assertThat(savedVenue.getLocation()).isNotNull();
        assertThat(savedVenue.getLocation().getAddress()).isEqualTo("Nevsky Prospect, 1");
    }

    @Test
    @DisplayName("Should find Venue by Name (case-insensitive)")
    void testFindByName() {
        venueRepository.save(defaultVenue);

        List<Venue> foundVenues = venueRepository.findByNameContainingIgnoreCase("hall");

        assertThat(foundVenues).isNotEmpty();
        assertThat(foundVenues).anyMatch(venue -> "Main Hall".equals(venue.getName()));
    }

    @Test
    @DisplayName("Should find Venue by Location")
    void testFindByLocation() {
        Venue savedVenue = venueRepository.save(defaultVenue);

        Optional<Venue> foundVenue = venueRepository.findByLocation(savedVenue.getLocation());

        assertThat(foundVenue).isPresent();
        assertThat(foundVenue.get().getName()).isEqualTo("Main Hall");
    }

    @Test
    @DisplayName("Should not find non-existent Venue by Name")
    void testFindNonExistentVenueByName() {
        List<Venue> foundVenues = venueRepository.findByNameContainingIgnoreCase("NonExistentVenue");

        assertThat(foundVenues).isEmpty();
    }

    @Test
    @DisplayName("Should not save Venue without Name")
    void testSaveVenueWithoutName() {
        defaultVenue.setName(null);

        assertThatThrownBy(() -> venueRepository.saveAndFlush(defaultVenue))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save Venue without Location")
    void testSaveVenueWithoutLocation() {
        defaultVenue.setLocation(null);

        assertThatThrownBy(() -> venueRepository.saveAndFlush(defaultVenue))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should save Venue with full details")
    void testSaveVenueWithFullDetails() {
        defaultVenue.setName("Conference Room");
        defaultVenue.setCapacity(100);

        Venue savedVenue = venueRepository.save(defaultVenue);

        assertThat(savedVenue).isNotNull();
        assertThat(savedVenue.getId()).isNotNull();
        assertThat(savedVenue.getName()).isEqualTo("Conference Room");
        assertThat(savedVenue.getCapacity()).isEqualTo(100);
        assertThat(savedVenue.getLocation().getAddress()).isEqualTo("Nevsky Prospect, 1");
    }
}


