package ru.itmo.cs.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;

@Transactional
@Rollback
class EventRepositoryTest extends IntegrationTestBase {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    private Event defaultEvent;

    @BeforeEach
    void setUp() {
        Location location = createAndSaveLocation();
        Venue venue = createAndSaveVenue("Test Venue", 100, location);
        Category category = createAndSaveCategory(CategoryEnum.SEMINAR);

        defaultEvent = new Event();
        defaultEvent.setName("Test Event");
        defaultEvent.setDescription("Test Description");
        defaultEvent.setDate(LocalDateTime.now().plusDays(1));
        defaultEvent.setVenue(venue);
        defaultEvent.setCategory(category);
        defaultEvent.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should save Event correctly")
    void testSaveEvent() {
        Event savedEvent = eventRepository.save(defaultEvent);

        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getName()).isEqualTo("Test Event");
        assertThat(savedEvent.getCategory().getName()).isEqualTo(CategoryEnum.SEMINAR);
        assertThat(savedEvent.getVenue().getName()).isEqualTo("Test Venue");
    }

    @Test
    @DisplayName("Should find Event by Name")
    void testFindEventByName() {
        eventRepository.save(defaultEvent);

        Event foundEvent = eventRepository.findByName("Test Event");

        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getName()).isEqualTo("Test Event");
    }

    @Test
    @DisplayName("Should find Events by Date Range")
    void testFindEventsByDateRange() {
        eventRepository.save(defaultEvent);

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        List<Event> events = eventRepository.findByDateBetween(startDate, endDate);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getName()).isEqualTo("Test Event");
    }

    @Test
    @DisplayName("Should find Events by Category Name")
    void testFindEventsByCategoryName() {
        eventRepository.save(defaultEvent);

        List<Event> events = eventRepository.findByCategoryName(CategoryEnum.SEMINAR);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getCategory().getName()).isEqualTo(CategoryEnum.SEMINAR);
    }

    @Test
    @DisplayName("Should find Event with Food")
    void testFindEventWithFood() {
        Food food = createAndSaveFood("Pizza");
        defaultEvent.getFood().add(food);
        Event savedEvent = eventRepository.save(defaultEvent);

        Optional<Event> foundEvent = eventRepository.findByIdWithFood(savedEvent.getId());

        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getFood()).hasSize(1);
        assertThat(foundEvent.get().getFood().get(0).getName()).isEqualTo("Pizza");
    }

    @Test
    @DisplayName("Should not save Event without Name")
    void testSaveEventWithoutName() {
        Event eventWithoutName = new Event();
        eventWithoutName.setDescription("Test Description");
        eventWithoutName.setDate(LocalDateTime.now().plusDays(1));
        eventWithoutName.setVenue(defaultEvent.getVenue());
        eventWithoutName.setCategory(defaultEvent.getCategory());
        eventWithoutName.setUpdatedAt(LocalDateTime.now());
        eventWithoutName.setName(null);

        assertThatThrownBy(() -> eventRepository.saveAndFlush(eventWithoutName))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save Event without Venue")
    void testSaveEventWithoutVenue() {
        Event eventWithoutVenue = new Event();
        eventWithoutVenue.setName("Test Event");
        eventWithoutVenue.setDescription("Test Description");
        eventWithoutVenue.setDate(LocalDateTime.now().plusDays(1));
        eventWithoutVenue.setCategory(defaultEvent.getCategory());
        eventWithoutVenue.setUpdatedAt(LocalDateTime.now());
        eventWithoutVenue.setVenue(null);

        assertThatThrownBy(() -> eventRepository.saveAndFlush(eventWithoutVenue))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save Event without Category")
    void testSaveEventWithoutCategory() {
        Event eventWithoutCategory = new Event();
        eventWithoutCategory.setName("Test Event");
        eventWithoutCategory.setDescription("Test Description");
        eventWithoutCategory.setDate(LocalDateTime.now().plusDays(1));
        eventWithoutCategory.setVenue(defaultEvent.getVenue());
        eventWithoutCategory.setUpdatedAt(LocalDateTime.now());
        eventWithoutCategory.setCategory(null);

        assertThatThrownBy(() -> eventRepository.saveAndFlush(eventWithoutCategory))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Location createAndSaveLocation(
        String country, String region, String city, String address) {
        Location location = new Location();
        location.setCountry(country);
        location.setRegion(region);
        location.setCity(city);
        location.setAddress(address);
        return locationRepository.save(location);
    }

    private Location createAndSaveLocation() {
        return createAndSaveLocation("Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1");
    }

    private Venue createAndSaveVenue(String name, int capacity, Location location) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setCapacity(capacity);
        venue.setLocation(location);
        return venueRepository.save(venue);
    }

    private Category createAndSaveCategory(CategoryEnum categoryEnum) {
        Category category = new Category();
        category.setName(categoryEnum);
        return categoryRepository.save(category);
    }

    private Food createAndSaveFood(String name) {
        Food food = new Food();
        food.setName(name);
        food.setComposition("Sample composition");
        return foodRepository.save(food);
    }
}
