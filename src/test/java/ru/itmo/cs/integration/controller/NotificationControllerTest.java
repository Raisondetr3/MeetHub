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
import ru.itmo.cs.dto.notification.NotificationDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VenueRepository venueRepository;

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
        notificationRepository.deleteAll();
        participantRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное создание уведомления")
    void shouldCreateNotificationSuccessfully() throws Exception {
        // Arrange
        NotificationDto notificationDto = new NotificationDto(
            null,
            defaultUser.getId(),
            defaultUser.getUsername(),
            defaultUser.getEmail(),
            defaultEvent.getId(),
            defaultEvent.getName(),
            "Test notification content",
            "SENT",
            null
        );

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/" + defaultEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificationDto))
                .header("Authorization", token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content", is("Test notification content")))
            .andExpect(jsonPath("$.status", is("SENT")))
            .andExpect(jsonPath("$.eventId", is(defaultEvent.getId())))
            .andExpect(jsonPath("$.userId", is(defaultUser.getId())));

        // Verify notification exists in the database
        List<Notification> notifications = notificationRepository.findByEventId(defaultEvent.getId());
        assertThat(notifications).hasSize(1);
    }

    @Test
    @DisplayName("Ошибка при создании уведомления для несуществующего мероприятия")
    void shouldFailToCreateNotificationForNonExistentEvent() throws Exception {
        // Arrange
        NotificationDto notificationDto = new NotificationDto(
            null,
            defaultUser.getId(),
            defaultUser.getUsername(),
            defaultUser.getEmail(),
            999,
            "Non-existent Event",
            "Test notification content",
            "SENT",
            null
        );

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificationDto))
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Мероприятие не найдено")));
    }

    @Test
    @DisplayName("Успешная отправка напоминаний")
    void shouldSendRemindersSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/" + defaultEvent.getId() + "/reminders")
                .header("Authorization", token))
            .andExpect(status().isOk());

        // Verify notifications exist in the database
        List<Notification> notifications = notificationRepository.findByEventId(defaultEvent.getId());
        assertThat(notifications).hasSize(1);
    }

    @Test
    @DisplayName("Ошибка при отправке напоминаний для несуществующего мероприятия")
    void shouldFailToSendRemindersForNonExistentEvent() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/999/reminders")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Мероприятие не найдено")));
    }

    @Test
    @DisplayName("Получение уведомлений для пользователя")
    void shouldGetNotificationsForUser() throws Exception {
        // Arrange
        Notification notification = new Notification();
        notification.setUser(defaultUser);
        notification.setEvent(defaultEvent);
        notification.setContent("Test notification content");
        notification.setStatus("SENT");
        notification.setSentAt(new Date());
        notificationRepository.save(notification);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/user/" + defaultUser.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()", is(1)))
            .andExpect(jsonPath("$[0].content", is("Test notification content")))
            .andExpect(jsonPath("$[0].status", is("SENT")));
    }

    @Test
    @DisplayName("Ошибка при получении уведомлений для несуществующего пользователя")
    void shouldFailToGetNotificationsForNonExistentUser() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/user/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Пользователь не найден")));
    }
}