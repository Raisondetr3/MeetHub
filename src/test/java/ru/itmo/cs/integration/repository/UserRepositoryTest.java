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
class UserRepositoryTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private User defaultUser;
    private Event defaultEvent;

    @BeforeEach
    void setUp() {
        defaultUser = createAndSaveUser("testuser", "testuser@test.com");
        defaultEvent = createAndSaveEvent("Test Event", "Test Description");
    }

    @Test
    @DisplayName("Should save User correctly")
    void testSaveUser() {
        User savedUser = userRepository.save(defaultUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@test.com");
    }

    @Test
    @DisplayName("Should find User by Username")
    void testFindUserByUsername() {
        userRepository.save(defaultUser);

        Optional<User> foundUser = userRepository.findByUsername("testuser");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should find Participants by Event ID")
    void testFindParticipantsByEventId() {
        Participant participant =
            new Participant(
                new Participant.ParticipantId(defaultUser.getId(), defaultEvent.getId()),
                defaultUser,
                defaultEvent,
                false);
        participantRepository.save(participant);

        List<User> participants = userRepository.findParticipantsByEventId(defaultEvent.getId());

        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getId()).isEqualTo(defaultUser.getId());
    }

    @Test
    @DisplayName("Should not save User with duplicate Email")
    void testSaveUserWithDuplicateEmail() {
        userRepository.save(defaultUser);

        User duplicateUser = new User();
        duplicateUser.setUsername("newuser");
        duplicateUser.setEmail("testuser@test.com");
        duplicateUser.setPassword("anotherpassword");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save User without Username")
    void testSaveUserWithoutUsername() {
        defaultUser.setUsername(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(defaultUser))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save User without Email")
    void testSaveUserWithoutEmail() {
        defaultUser.setEmail(null);

        assertThatThrownBy(() -> userRepository.saveAndFlush(defaultUser))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User createAndSaveUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("securepassword");
        return userRepository.save(user);
    }

    private Event createAndSaveEvent(String name, String description) {
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setDate(LocalDateTime.now().plusDays(1));
        event.setVenue(createAndSaveVenue("Test Venue", 100, createAndSaveLocation()));
        event.setCategory(createAndSaveCategory(CategoryEnum.SEMINAR));
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    private Venue createAndSaveVenue(String name, int capacity, Location location) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setCapacity(capacity);
        venue.setLocation(location);
        return venueRepository.save(venue);
    }

    private Location createAndSaveLocation() {
        Location location = new Location();
        location.setCountry("Russia");
        location.setRegion("Saint Petersburg");
        location.setCity("SPB");
        location.setAddress("Nevsky Prospect, 1");
        return locationRepository.save(location);
    }

    private Category createAndSaveCategory(CategoryEnum categoryEnum) {
        Category category = new Category();
        category.setName(categoryEnum);
        return categoryRepository.save(category);
    }
}
