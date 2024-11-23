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
import ru.itmo.cs.repository.LocationRepository;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@Rollback
class LocationRepositoryTest extends IntegrationTestBase {

    @Autowired
    private LocationRepository locationRepository;

    private Location defaultLocation;

    @BeforeEach
    void setUp() {
        defaultLocation = new Location();
        defaultLocation.setCountry("Russia");
        defaultLocation.setRegion("Saint Petersburg");
        defaultLocation.setCity("SPB");
        defaultLocation.setAddress("Nevsky Prospect, 1");
    }

    @Test
    @DisplayName("Should save Location correctly")
    void testSaveLocation() {
        Location savedLocation = locationRepository.save(defaultLocation);

        assertThat(savedLocation).isNotNull();
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getCountry()).isEqualTo("Russia");
        assertThat(savedLocation.getCity()).isEqualTo("SPB");
    }

    @Test
    @DisplayName("Should find Location by Country")
    void testFindByCountry() {
        locationRepository.save(defaultLocation);

        List<Location> foundLocations = locationRepository.findByCountry("Russia");

        assertThat(foundLocations).isNotEmpty();
        assertThat(foundLocations).anyMatch(location -> location.getCity().equals("SPB"));
    }

    @Test
    @DisplayName("Should find Location by City")
    void testFindByCity() {
        locationRepository.save(defaultLocation);

        List<Location> foundLocations = locationRepository.findByCity("SPB");

        assertThat(foundLocations).isNotEmpty();
        assertThat(foundLocations).anyMatch(location -> location.getCountry().equals("Russia"));
    }

    @Test
    @DisplayName("Should not find non-existent Location by Country")
    void testFindNonExistentLocationByCountry() {
        List<Location> foundLocations = locationRepository.findByCountry("NonExistentCountry");

        assertThat(foundLocations).isEmpty();
    }

    @Test
    @DisplayName("Should not save Location without Country")
    void testSaveLocationWithoutCountry() {
        defaultLocation.setCountry(null);

        assertThatThrownBy(() -> locationRepository.saveAndFlush(defaultLocation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save duplicate Locations with the same Address")
    void testSaveDuplicateLocation() {
        locationRepository.save(defaultLocation);

        Location duplicateLocation = new Location();
        duplicateLocation.setCountry("Russia");
        duplicateLocation.setRegion("Saint Petersburg");
        duplicateLocation.setCity("SPB");
        duplicateLocation.setAddress("Nevsky Prospect, 15");

        Location savedDuplicateLocation = locationRepository.save(duplicateLocation);

        assertThat(savedDuplicateLocation).isNotNull();
        assertThat(savedDuplicateLocation.getId()).isNotNull();
        assertThat(savedDuplicateLocation.getCountry()).isEqualTo("Russia");
        assertThat(savedDuplicateLocation.getCity()).isEqualTo("SPB");
    }

    @Test
    @DisplayName("Should save and retrieve Location with full details")
    void testSaveLocationWithFullDetails() {
        defaultLocation.setAddress("Lenina Street, 10");

        Location savedLocation = locationRepository.save(defaultLocation);

        assertThat(savedLocation).isNotNull();
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getAddress()).isEqualTo("Lenina Street, 10");
    }
}

