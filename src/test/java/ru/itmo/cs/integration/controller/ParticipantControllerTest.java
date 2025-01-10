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
import ru.itmo.cs.dto.participant.ParticipantDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ParticipantControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private LocationRepository locationRepository;

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
        participantRepository.deleteAll();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        locationRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешная регистрация участника в мероприятии")
    void shouldRegisterParticipantSuccessfully() throws Exception {
        // Arrange
        ParticipantDto participantDto = new ParticipantDto(
            defaultUser.getId(),
            defaultUser.getUsername(),
            defaultUser.getEmail(),
            false
        );
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/participants/" + defaultEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(objectMapper.writeValueAsString(participantDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId", is(defaultUser.getId())))
            .andExpect(jsonPath("$.username", is(defaultUser.getUsername())))
            .andExpect(jsonPath("$.email", is(defaultUser.getEmail())))
            .andExpect(jsonPath("$.isCreator", is(false)));
    }

    @Test
    @DisplayName("Успешная регистрация организатора для мероприятия")
    void shouldRegisterOrganizerSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/participants/" + defaultEvent.getId() + "/organizer/" + defaultUser.getId())
                .header("Authorization", token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId", is(defaultUser.getId())))
            .andExpect(jsonPath("$.username", is(defaultUser.getUsername())))
            .andExpect(jsonPath("$.email", is(defaultUser.getEmail())))
            .andExpect(jsonPath("$.isCreator", is(true)));
    }

    @Test
    @DisplayName("Получение списка участников для мероприятия")
    void shouldGetParticipantsByEvent() throws Exception {
        // Arrange
        Participant participant = new Participant(
            new Participant.ParticipantId(defaultUser.getId(), defaultEvent.getId()),
            defaultUser,
            defaultEvent,
            false
        );
        participantRepository.save(participant);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/participants/" + defaultEvent.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()", is(1)))
            .andExpect(jsonPath("$[0].userId", is(defaultUser.getId())))
            .andExpect(jsonPath("$[0].username", is(defaultUser.getUsername())))
            .andExpect(jsonPath("$[0].email", is(defaultUser.getEmail())))
            .andExpect(jsonPath("$[0].isCreator", is(false)));
    }

    @Test
    @DisplayName("Удаление участника из мероприятия")
    void shouldRemoveParticipantSuccessfully() throws Exception {
        // Arrange
        Participant participant = new Participant(
            new Participant.ParticipantId(defaultUser.getId(), defaultEvent.getId()),
            defaultUser,
            defaultEvent,
            false
        );
        participantRepository.save(participant);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/participants/" + defaultEvent.getId() + "/user/" + defaultUser.getId())
                .header("Authorization", token))
            .andExpect(status().isNoContent());

        // Verify participant was removed
        assertThat(participantRepository.findById(new Participant.ParticipantId(defaultUser.getId(), defaultEvent.getId())))
            .isEmpty();
    }

    @Test
    @DisplayName("Ошибка при регистрации участника для несуществующего мероприятия")
    void shouldFailToRegisterParticipantForNonExistentEvent() throws Exception {
        // Arrange
        ParticipantDto participantDto = new ParticipantDto(
            defaultUser.getId(),
            defaultUser.getUsername(),
            defaultUser.getEmail(),
            false
        );

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/participants/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(objectMapper.writeValueAsString(participantDto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего участника")
    void shouldFailToRemoveNonExistentParticipant() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/participants/" + defaultEvent.getId() + "/user/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound());
    }
}