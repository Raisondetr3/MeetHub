package ru.itmo.cs.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
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
class ReviewRepositoryTest extends IntegrationTestBase {

  @Autowired private ReviewRepository reviewRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private VenueRepository venueRepository;

  @Autowired private LocationRepository locationRepository;

  @Autowired private CategoryRepository categoryRepository;

  private User defaultUser;
  private Event defaultEvent;
  private Review defaultReview;

  @BeforeEach
  void setUp() {
    defaultUser = createAndSaveUser("testuser", "testuser@test.com");
    defaultEvent = createAndSaveEvent("Test Event", "Test Description");

    defaultReview = new Review();
    defaultReview.setUser(defaultUser);
    defaultReview.setEvent(defaultEvent);
    defaultReview.setRating(Rating.FIVE_STARS);
    defaultReview.setComment("Excellent event!");
  }

  @Test
  @DisplayName("Should save Review correctly")
  void testSaveReview() {
    Review savedReview = reviewRepository.save(defaultReview);

    assertThat(savedReview).isNotNull();
    assertThat(savedReview.getId()).isNotNull();
    assertThat(savedReview.getUser()).isEqualTo(defaultUser);
    assertThat(savedReview.getEvent()).isEqualTo(defaultEvent);
    assertThat(savedReview.getRating()).isEqualTo(Rating.FIVE_STARS);
    assertThat(savedReview.getComment()).isEqualTo("Excellent event!");
  }

  @Test
  @DisplayName("Should find Reviews by User")
  void testFindReviewsByUser() {
    reviewRepository.save(defaultReview);

    List<Review> reviews = reviewRepository.findByUser(defaultUser);

    assertThat(reviews).hasSize(1);
    assertThat(reviews.get(0).getUser()).isEqualTo(defaultUser);
  }

  @Test
  @DisplayName("Should find Reviews by Event")
  void testFindReviewsByEvent() {
    reviewRepository.save(defaultReview);

    List<Review> reviews = reviewRepository.findByEvent(defaultEvent);

    assertThat(reviews).hasSize(1);
    assertThat(reviews.get(0).getEvent()).isEqualTo(defaultEvent);
  }

  @Test
  @DisplayName("Should find Reviews by Event and Rating")
  void testFindReviewsByEventAndRating() {
    reviewRepository.save(defaultReview);

    List<Review> reviews = reviewRepository.findByEventAndRating(defaultEvent, Rating.FIVE_STARS);

    assertThat(reviews).hasSize(1);
    assertThat(reviews.get(0).getRating()).isEqualTo(Rating.FIVE_STARS);
  }

  @Test
  @DisplayName("Should not save Review without User")
  void testSaveReviewWithoutUser() {
    defaultReview.setUser(null);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(defaultReview))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("Should not save Review without Event")
  void testSaveReviewWithoutEvent() {
    defaultReview.setEvent(null);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(defaultReview))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("Should not save Review without Rating")
  void testSaveReviewWithoutRating() {
    defaultReview.setRating(null);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(defaultReview))
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
