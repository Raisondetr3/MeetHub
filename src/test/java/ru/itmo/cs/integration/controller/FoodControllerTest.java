package ru.itmo.cs.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FoodControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired CategoryRepository categoryRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    ParticipantRepository participantRepository;

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


    @AfterEach
    void tearDown() {
        foodRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Location createAndSaveLocation() {
        Location location = new Location();
        location.setCountry("Russia");
        location.setRegion("Saint Petersburg");
        location.setCity("SPB");
        location.setAddress("Nevsky Prospect, 1");
        return locationRepository.save(location);
    }

//    @Test
//    @Disabled
//    @DisplayName("Успешное создание списка еды")
//    void shouldCreateFoodListSuccessfully() throws Exception {
//        // Arrange
//        List<FoodDto> foodDtos = List.of(
//            new FoodDto(null, "Pizza", "Cheese, Tomato, Dough"),
//            new FoodDto(null, "Pasta", "Flour, Eggs, Olive Oil")
//        );
//
//        String token = generateToken(defaultUser);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/food/" + defaultEvent.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(foodDtos))
//                .header("Authorization", token))
//            .andExpect(status().isCreated())
//            .andExpect(jsonPath("$.size()", Matchers.is(2)))
//            .andExpect(jsonPath("$[0].name", Matchers.is("Pizza")))
//            .andExpect(jsonPath("$[1].name", Matchers.is("Pasta")));
//
//        // Verify in database
//        List<Food> savedFood = foodRepository.findAll();
//        assertEquals(2, savedFood.size());
//    }

    @Test
    @DisplayName("Успешное получение блюда по ID")
    void shouldGetFoodByIdSuccessfully() throws Exception {
        // Arrange
        Food food = new Food("Pizza", "Cheese, Tomato, Dough");
        food = foodRepository.save(food);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/food/" + food.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", Matchers.is("Pizza")))
            .andExpect(jsonPath("$.composition", Matchers.is("Cheese, Tomato, Dough")));
    }

    @Test
    @DisplayName("Ошибка при получении несуществующего блюда")
    void shouldFailToGetNonExistentFood() throws Exception {
        // Arrange
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/food/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", Matchers.is("Food not found with ID: 999")));
    }

//    @Test
//    @Disabled
//    @DisplayName("Успешное обновление блюда")
//    void shouldUpdateFoodSuccessfully() throws Exception {
//        // Arrange
//        Food food = new Food("Pizza", "Cheese, Tomato, Dough");
//        food = foodRepository.save(food);
//
//        FoodDto updatedFoodDto = new FoodDto(food.getId(), "Pizza Updated", "Cheese, Tomato, Basil");
//        String token = generateToken(defaultUser);
//
//        // Act & Assert
//        mockMvc.perform(put("/api/v1/food/" + defaultEvent.getId() + "/" + food.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(updatedFoodDto))
//                .header("Authorization", token))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.name", Matchers.is("Pizza Updated")))
//            .andExpect(jsonPath("$.composition", Matchers.is("Cheese, Tomato, Basil")));
//
//        // Verify in database
//        Food updatedFood = foodRepository.findById(food.getId()).orElseThrow();
//        assertEquals("Pizza Updated", updatedFood.getName());
//        assertEquals("Cheese, Tomato, Basil", updatedFood.getComposition());
//    }

//    @Test
//    @Disabled
//    @DisplayName("Успешное удаление блюда")
//    void shouldDeleteFoodSuccessfully() throws Exception {
//        // Arrange
//        Food food = new Food("Pizza", "Cheese, Tomato, Dough");
//        food = foodRepository.save(food);
//
//        defaultEvent.getFood().add(food);
//        eventRepository.save(defaultEvent);
//
//        String token = generateToken(defaultUser);
//
//        // Act & Assert
//        mockMvc.perform(delete("/api/v1/food/" + defaultEvent.getId() + "/" + food.getId())
//                .header("Authorization", token))
//            .andExpect(status().isNoContent());
//
//        // Verify in database
//        assertFalse(foodRepository.existsById(food.getId()));
//    }

    @Test
    @DisplayName("Поиск блюд по названию")
    void shouldSearchFoodByNameSuccessfully() throws Exception {
        // Arrange
        Food food1 = new Food("Pizza", "Cheese, Tomato, Dough");
        Food food2 = new Food("Pasta", "Flour, Eggs, Olive Oil");
        foodRepository.saveAll(List.of(food1, food2));

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/food/search?name=Piz")
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()", Matchers.is(1)))
            .andExpect(jsonPath("$[0].name", Matchers.is("Pizza")));
    }
}