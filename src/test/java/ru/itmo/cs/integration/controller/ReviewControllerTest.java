package ru.itmo.cs.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.itmo.cs.dto.review.ReviewDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User defaultUser;
    private Event defaultEvent;

    @BeforeEach
    void setUp() {
        defaultUser = new User();
        defaultUser.setUsername("testUser");
        defaultUser.setEmail("example@mail.ru");
        defaultUser.setPassword("password");
        defaultUser = userRepository.save(defaultUser);

        Category defaultCategory = new Category();
        defaultCategory.setName(CategoryEnum.SEMINAR);
        defaultCategory = categoryRepository.save(defaultCategory);

        Venue defaultVenue = new Venue();
        defaultVenue.setName("Test Venue");
        defaultVenue.setCapacity(100);
        defaultVenue.setLocation(createAndSaveLocation());
        defaultVenue = venueRepository.save(defaultVenue);

        defaultEvent = new Event();
        defaultEvent.setName("Test Event");
        defaultEvent.setDescription("Test Description");
        defaultEvent.setDate(LocalDateTime.now().plusDays(1));
        defaultEvent.setUpdatedAt(LocalDateTime.now());
        defaultEvent.setCategory(defaultCategory);
        defaultEvent.setVenue(defaultVenue);
        defaultEvent = eventRepository.save(defaultEvent);

        Participant participant = new Participant();
        participant.setId(new Participant.ParticipantId(defaultUser.getId(), defaultEvent.getId()));
        participant.setUser(defaultUser);
        participant.setEvent(defaultEvent);
        participant.setIsCreator(true);
        participantRepository.save(participant);
    }

    private Location createAndSaveLocation() {
        Location location = new Location();
        location.setCountry("Russia");
        location.setRegion("Saint Petersburg");
        location.setCity("SPB");
        location.setAddress("Nevsky Prospect, 1");
        return locationRepository.save(location);
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
        participantRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение отзывов пользователя")
    void shouldGetReviewsByUser() throws Exception {
        // Arrange
        Review review = new Review();
        review.setUser(defaultUser);
        review.setEvent(defaultEvent);
        review.setRating(Rating.FIVE_STARS);
        review.setComment("Great event!");
        review.setCreatedAt(new Date());
        reviewRepository.save(review);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reviews/user/" + defaultUser.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()", is(1)))
            .andExpect(jsonPath("$[0].comment", is("Great event!")))
            .andExpect(jsonPath("$[0].rating", is("FIVE_STARS")));
    }

    @Test
    @DisplayName("Получение отзывов мероприятия")
    void shouldGetReviewsByEvent() throws Exception {
        // Arrange
        Review review = new Review();
        review.setUser(defaultUser);
        review.setEvent(defaultEvent);
        review.setRating(Rating.FIVE_STARS);
        review.setComment("Amazing experience!");
        review.setCreatedAt(new Date());
        reviewRepository.save(review);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reviews/event/" + defaultEvent.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()", is(1)))
            .andExpect(jsonPath("$[0].comment", is("Amazing experience!")))
            .andExpect(jsonPath("$[0].rating", is("FIVE_STARS")));
    }

    @Test
    @DisplayName("Удаление отзыва")
    void shouldDeleteReviewSuccessfully() throws Exception {
        // Arrange
        Review review = new Review();
        review.setUser(defaultUser);
        review.setEvent(defaultEvent);
        review.setRating(Rating.FOUR_STARS);
        review.setComment("Good event");
        review.setCreatedAt(new Date());
        review = reviewRepository.save(review);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/reviews/" + review.getId())
                .header("Authorization", token))
            .andExpect(status().isNoContent());

        Optional<Review> deletedReview = reviewRepository.findById(review.getId());
        assertThat(deletedReview).isEmpty();
    }
}
