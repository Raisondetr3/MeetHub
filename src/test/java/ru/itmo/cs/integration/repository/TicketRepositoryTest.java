package ru.itmo.cs.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.dao.DataIntegrityViolationException;
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
class TicketRepositoryTest extends IntegrationTestBase {

    @Autowired
    private TicketRepository ticketRepository;

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
    private Ticket defaultTicket;

    @BeforeEach
    void setUp() {
        defaultUser = createAndSaveUser("testuser", "testuser@test.com");
        defaultEvent = createAndSaveEvent("Test Event", "Test Description");

        defaultTicket = new Ticket();
        defaultTicket.setSeatNumber("A1");
        defaultTicket.setUser(defaultUser);
        defaultTicket.setEvent(defaultEvent);
    }

    @Test
    @DisplayName("Should save Ticket correctly")
    void testSaveTicket() {
        Ticket savedTicket = ticketRepository.save(defaultTicket);

        assertThat(savedTicket).isNotNull();
        assertThat(savedTicket.getId()).isNotNull();
        assertThat(savedTicket.getSeatNumber()).isEqualTo("A1");
        assertThat(savedTicket.getUser()).isEqualTo(defaultUser);
        assertThat(savedTicket.getEvent()).isEqualTo(defaultEvent);
    }

    @Test
    @DisplayName("Should find Tickets by User")
    void testFindTicketsByUser() {
        ticketRepository.save(defaultTicket);

        List<Ticket> tickets = ticketRepository.findByUser(defaultUser);

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getUser()).isEqualTo(defaultUser);
    }

    @Test
    @DisplayName("Should find Tickets by Event")
    void testFindTicketsByEvent() {
        ticketRepository.save(defaultTicket);

        List<Ticket> tickets = ticketRepository.findByEvent(defaultEvent);

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getEvent()).isEqualTo(defaultEvent);
    }

    @Test
    @DisplayName("Should find Ticket by Seat Number and Event")
    void testFindTicketBySeatNumberAndEvent() {
        ticketRepository.save(defaultTicket);

        Optional<Ticket> foundTicket = ticketRepository.findBySeatNumberAndEvent("A1", defaultEvent);

        assertThat(foundTicket).isPresent();
        assertThat(foundTicket.get().getSeatNumber()).isEqualTo("A1");
        assertThat(foundTicket.get().getEvent()).isEqualTo(defaultEvent);
    }

    @Test
    @DisplayName("Should not save Ticket without User")
    void testSaveTicketWithoutUser() {
        defaultTicket.setUser(null);

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(defaultTicket))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save Ticket without Event")
    void testSaveTicketWithoutEvent() {
        defaultTicket.setEvent(null);

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(defaultTicket))
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

