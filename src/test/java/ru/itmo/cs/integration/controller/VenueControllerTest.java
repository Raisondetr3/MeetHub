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
import ru.itmo.cs.dto.location.LocationDto;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VenueControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    private User defaultUser;
    private Location defaultLocation;

    @BeforeEach
    void setUp() {
        defaultUser = new User();
        defaultUser.setUsername("testUser");
        defaultUser.setEmail("example@mail.ru");
        defaultUser.setPassword("password");
        defaultUser = userRepository.save(defaultUser);

        defaultLocation = new Location();
        defaultLocation.setCountry("Russia");
        defaultLocation.setRegion("Saint Petersburg");
        defaultLocation.setCity("SPB");
        defaultLocation.setAddress("Nevsky Prospect, 1");
        defaultLocation = locationRepository.save(defaultLocation);
    }

    @AfterEach
    void tearDown() {
        venueRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное создание места проведения")
    void shouldCreateVenueSuccessfully() throws Exception {
        // Arrange
        VenueDto venueDto = new VenueDto(null, "Test Venue", 100,
            new LocationDto(defaultLocation.getId(), "Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1"));

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(venueDto))
                .header("Authorization", token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Test Venue")))
            .andExpect(jsonPath("$.capacity", is(100)))
            .andExpect(jsonPath("$.location.country", is("Russia")));
    }

    @Test
    @DisplayName("Успешное получение места проведения по ID")
    void shouldGetVenueByIdSuccessfully() throws Exception {
        // Arrange
        Venue venue = new Venue(null, "Test Venue", defaultLocation, 100);
        venue = venueRepository.save(venue);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/venues/" + venue.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Test Venue")))
            .andExpect(jsonPath("$.capacity", is(100)))
            .andExpect(jsonPath("$.location.country", is("Russia")));
    }

    @Test
    @DisplayName("Ошибка при получении места проведения с неверным ID")
    void shouldReturnNotFoundForInvalidVenueId() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/venues/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Venue not found")));
    }

    @Test
    @DisplayName("Успешное обновление места проведения")
    void shouldUpdateVenueSuccessfully() throws Exception {
        // Arrange
        Venue venue = new Venue(null, "Old Venue", defaultLocation, 100);
        venue = venueRepository.save(venue);

        VenueDto updatedVenueDto = new VenueDto(null, "Updated Venue", 150,
            new LocationDto(defaultLocation.getId(), "Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1"));

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/venues/" + venue.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedVenueDto))
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Updated Venue")))
            .andExpect(jsonPath("$.capacity", is(150)));
    }

    @Test
    @DisplayName("Успешное удаление места проведения")
    void shouldDeleteVenueSuccessfully() throws Exception {
        // Arrange
        Venue venue = new Venue(null, "Test Venue", defaultLocation, 100);
        venue = venueRepository.save(venue);

        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/venues/" + venue.getId())
                .header("Authorization", token))
            .andExpect(status().isNoContent());

        assertThat(venueRepository.findById(venue.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ошибка при удалении места проведения с неверным ID")
    void shouldReturnNotFoundForInvalidVenueIdOnDelete() throws Exception {
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/venues/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Venue not found")));
    }
}