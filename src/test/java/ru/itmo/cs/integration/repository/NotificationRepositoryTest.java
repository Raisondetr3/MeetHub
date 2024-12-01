package ru.itmo.cs.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Date;
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
class NotificationRepositoryTest extends IntegrationTestBase {

  @Autowired private NotificationRepository notificationRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private VenueRepository venueRepository;

  @Autowired private LocationRepository locationRepository;
  @Autowired private CategoryRepository categoryRepository;

  private User defaultUser;
  private Event defaultEvent;
  private Notification defaultNotification;

  @BeforeEach
  void setUp() {
    defaultUser = createAndSaveUser("testuser", "testuser@test.com");
    defaultEvent = createAndSaveEvent("Test Event", "Test Description");

    defaultNotification = new Notification();
    defaultNotification.setUser(defaultUser);
    defaultNotification.setEvent(defaultEvent);
    defaultNotification.setContent("Test notification content");
  }

  @Test
  @DisplayName("Should save Notification correctly")
  void testSaveNotification() {
    Notification savedNotification = notificationRepository.save(defaultNotification);

    assertThat(savedNotification).isNotNull();
    assertThat(savedNotification.getId()).isNotNull();
    assertThat(savedNotification.getUser()).isEqualTo(defaultUser);
    assertThat(savedNotification.getEvent()).isEqualTo(defaultEvent);
    assertThat(savedNotification.getContent()).isEqualTo("Test notification content");
  }

  @Test
  @DisplayName("Should find Notifications by User")
  void testFindNotificationsByUser() {
    notificationRepository.save(defaultNotification);

    List<Notification> notifications = notificationRepository.findByUser(defaultUser);

    assertThat(notifications).hasSize(1);
    assertThat(notifications.get(0).getUser()).isEqualTo(defaultUser);
  }

  @Test
  @DisplayName("Should find Notifications by Event")
  void testFindNotificationsByEvent() {
    notificationRepository.save(defaultNotification);

    List<Notification> notifications = notificationRepository.findByEvent(defaultEvent);

    assertThat(notifications).hasSize(1);
    assertThat(notifications.get(0).getEvent()).isEqualTo(defaultEvent);
  }

  @Test
  @DisplayName("Should find Notifications by User ordered by sent date descending")
  void testFindNotificationsByUserOrderedBySentAt() {
    Notification earlierNotification = new Notification();
    earlierNotification.setUser(defaultUser);
    earlierNotification.setEvent(defaultEvent);
    earlierNotification.setContent("Earlier notification content");
    earlierNotification.setSentAt(new Date(1000));

    notificationRepository.saveAndFlush(earlierNotification);

    defaultNotification.setSentAt(new Date(2000));
    notificationRepository.saveAndFlush(defaultNotification);

    List<Notification> notifications =
        notificationRepository.findByUserOrderBySentAtDesc(defaultUser);


    assertThat(notifications).hasSize(2);
    assertThat(notifications.get(0).getContent()).isEqualTo("Test notification content");
    assertThat(notifications.get(1).getContent()).isEqualTo("Earlier notification content");
  }


  @Test
  @DisplayName("Should not save Notification without User")
  void testSaveNotificationWithoutUser() {
    defaultNotification.setUser(null);

    assertThatThrownBy(() -> notificationRepository.saveAndFlush(defaultNotification))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("Should not save Notification without Event")
  void testSaveNotificationWithoutEvent() {
    defaultNotification.setEvent(null);

    assertThatThrownBy(() -> notificationRepository.saveAndFlush(defaultNotification))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("Should not save Notification without Content")
  void testSaveNotificationWithoutContent() {
    defaultNotification.setContent(null);

    assertThatThrownBy(() -> notificationRepository.saveAndFlush(defaultNotification))
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
