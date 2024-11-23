package ru.itmo.cs.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itmo.cs.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@Rollback
class ParticipantRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ParticipantRepository participantRepository;

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

    private User defaultUser;
    private Event defaultEvent;
    private Participant defaultParticipant;

    @BeforeEach
    void setUp() {
        defaultUser = createAndSaveUser("testuser", "testuser@test.com");
        defaultEvent = createAndSaveEvent("Test Event", "Test Description");

        Participant.ParticipantId participantId = new Participant.ParticipantId(defaultUser.getId(), defaultEvent.getId());

        defaultParticipant = new Participant();
        defaultParticipant.setId(participantId);
        defaultParticipant.setUser(defaultUser);
        defaultParticipant.setEvent(defaultEvent);
        defaultParticipant.setIsCreator(true);
    }

    @Test
    @DisplayName("Should save Participant correctly")
    void testSaveParticipant() {
        Participant savedParticipant = participantRepository.save(defaultParticipant);

        assertThat(savedParticipant).isNotNull();
        assertThat(savedParticipant.getId()).isEqualTo(defaultParticipant.getId());
        assertThat(savedParticipant.getUser()).isEqualTo(defaultUser);
        assertThat(savedParticipant.getEvent()).isEqualTo(defaultEvent);
        assertThat(savedParticipant.getIsCreator()).isTrue();
    }

    @Test
    @DisplayName("Should find Participants by Event")
    void testFindParticipantsByEvent() {
        participantRepository.save(defaultParticipant);

        List<Participant> participants = participantRepository.findByEvent(defaultEvent);

        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getEvent()).isEqualTo(defaultEvent);
    }

    @Test
    @DisplayName("Should find Participants by User")
    void testFindParticipantsByUser() {
        participantRepository.save(defaultParticipant);

        List<Participant> participants = participantRepository.findByUser(defaultUser);

        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getUser()).isEqualTo(defaultUser);
    }

    @Test
    @DisplayName("Should find Event Creator")
    void testFindEventCreator() {
        participantRepository.save(defaultParticipant);

        Optional<Participant> eventCreator = participantRepository.findEventCreator(defaultEvent);

        assertThat(eventCreator).isPresent();
        assertThat(eventCreator.get().getIsCreator()).isTrue();
        assertThat(eventCreator.get().getUser()).isEqualTo(defaultUser);
    }

    @Test
    @DisplayName("Should not save Participant without User")
    void testSaveParticipantWithoutUser() {
        defaultParticipant.setUser(null);

        assertThatThrownBy(() -> participantRepository.saveAndFlush(defaultParticipant))
                .isInstanceOf(JpaSystemException.class)
                .hasMessageContaining("attempted to assign id from null one-to-one property");
    }

    @Test
    @DisplayName("Should not save Participant without Event")
    void testSaveParticipantWithoutEvent() {
        defaultParticipant.setEvent(null);

        assertThatThrownBy(() -> participantRepository.saveAndFlush(defaultParticipant))
                .isInstanceOf(JpaSystemException.class)
                .hasMessageContaining("attempted to assign id from null one-to-one property");
    }

    @Test
    @DisplayName("Should not save Participant without Creator flag")
    void testSaveParticipantWithoutCreatorFlag() {
        defaultParticipant.setIsCreator(null);

        assertThatThrownBy(() -> participantRepository.saveAndFlush(defaultParticipant))
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

