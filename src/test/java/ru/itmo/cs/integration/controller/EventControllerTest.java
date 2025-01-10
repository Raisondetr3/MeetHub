package ru.itmo.cs.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.itmo.cs.dto.category.CategoryDto;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.location.LocationDto;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

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
        participantRepository.deleteAll();
        eventRepository.deleteAll();
        categoryRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное получение мероприятий с фильтрацией и пагинацией")
    void shouldGetAllEventsSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/v1/events")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.size()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Test Event"));
    }

    @Test
    @DisplayName("Успешное получение мероприятия по ID")
    void shouldGetEventByIdSuccessfully() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/v1/events/" + defaultEvent.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Event"))
            .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("Ошибка при получении несуществующего мероприятия по ID")
    void shouldFailToGetNonExistentEventById() throws Exception {
        String token = generateToken(defaultUser);

        mockMvc.perform(get("/api/v1/events/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Мероприятие не найдено"));
    }

//    @Test
//    @Disabled
//    @DisplayName("Успешное создание нового мероприятия")
//    void shouldCreateEventSuccessfully() throws Exception {
//        LocationDto locationDto = new LocationDto(
//            1,
//            "Россия",
//            "Москва",
//            "Москва",
//            "ул. Ленина, 12"
//        );
//
//        VenueDto venueDto = new VenueDto(
//            1,
//            "Главный зал",
//            500,
//            locationDto
//        );
//
//        CategoryDto categoryDto = new CategoryDto();
//        categoryDto.setName("CONFERENCE");
//
//        EventDto eventDto = new EventDto();
//        eventDto.setName("Test Event");
//        eventDto.setDescription("Test Event Description");
//        eventDto.setDate(LocalDateTime.now().plusDays(1));
//        eventDto.setVenue(venueDto);
//        eventDto.setCategory(categoryDto);
//
//        String token = generateToken(defaultUser);
//
//        mockMvc.perform(post("/api/v1/events")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(eventDto))
//                .header("Authorization", token))
//            .andExpect(status().isCreated())
//            .andExpect(jsonPath("$.name").value("New Event"))
//            .andExpect(jsonPath("$.description").value("Description"));
//    }

//    @Test
//    @Disabled
//    @DisplayName("Успешное обновление мероприятия")
//    void shouldUpdateEventSuccessfully() throws Exception {
//        EventDto eventDto = new EventDto(defaultEvent.getId(), "Updated Event", "Updated Description",
//            defaultEvent.getDate().plusDays(1), null, null, null);
//
//        String token = generateToken(defaultUser);
//
//        mockMvc.perform(put("/api/v1/events/" + defaultEvent.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(eventDto))
//                .header("Authorization", token))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.name").value("Updated Event"))
//            .andExpect(jsonPath("$.description").value("Updated Description"));
//    }

//    @Test
//    @Disabled
//    @DisplayName("Успешное удаление мероприятия")
//    void shouldDeleteEventSuccessfully() throws Exception {
//        String token = generateToken(defaultUser);
//
//        mockMvc.perform(delete("/api/v1/events/" + defaultEvent.getId())
//                .header("Authorization", token))
//            .andExpect(status().isNoContent());
//
//        assertFalse(eventRepository.existsById(defaultEvent.getId()));
//    }
}
