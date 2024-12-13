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
import ru.itmo.cs.entity.*;
import ru.itmo.cs.dto.location.LocationDto;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LocationControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    private User defaultUser;
    private String token;

    @BeforeEach
    void setUp() {
        defaultUser = new User();
        defaultUser.setUsername("testUser");
        defaultUser.setEmail("test@example.com");
        defaultUser.setPassword("password");
        defaultUser = userRepository.save(defaultUser);

        token = generateToken(defaultUser);
    }

    @AfterEach
    void tearDown() {
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное создание местоположения")
    void shouldCreateLocationSuccessfully() throws Exception {
        // Arrange
        LocationDto locationDto = new LocationDto(null, "Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1");

        // Act & Assert
        mockMvc.perform(post("/api/v1/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationDto))
                .header("Authorization", token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.country", is("Russia")))
            .andExpect(jsonPath("$.city", is("SPB")));
    }

    @Test
    @DisplayName("Ошибка при создании местоположения с пустым адресом")
    void shouldFailToCreateLocationWithEmptyAddress() throws Exception {
        // Arrange
        LocationDto locationDto = new LocationDto(null, "Russia", "Moscow", "Moscow", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationDto))
                .header("Authorization", token))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешное получение местоположения по ID")
    void shouldGetLocationByIdSuccessfully() throws Exception {
        // Arrange
        Location location = new Location(null, "Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1");
        location = locationRepository.save(location);

        // Act & Assert
        mockMvc.perform(get("/api/v1/locations/" + location.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.country", is("Russia")))
            .andExpect(jsonPath("$.city", is("SPB")));
    }

    @Test
    @DisplayName("Ошибка при получении несуществующего местоположения")
    void shouldFailToGetNonExistentLocation() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/locations/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Location not found with ID: 999")));
    }

    @Test
    @DisplayName("Успешное обновление местоположения")
    void shouldUpdateLocationSuccessfully() throws Exception {
        // Arrange
        Location location = new Location(null, "Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1");
        location = locationRepository.save(location);

        LocationDto updatedLocationDto = new LocationDto(location.getId(), "Russia", "Moscow Region", "Moscow", "Tverskaya Street, 5");

        // Act & Assert
        mockMvc.perform(put("/api/v1/locations/" + location.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedLocationDto))
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.city", is("Moscow")))
            .andExpect(jsonPath("$.address", is("Tverskaya Street, 5")));
    }

    @Test
    @DisplayName("Ошибка при обновлении несуществующего местоположения")
    void shouldFailToUpdateNonExistentLocation() throws Exception {
        // Arrange
        LocationDto locationDto = new LocationDto(999, "Russia", "Moscow", "Moscow", "Tverskaya Street, 5");

        // Act & Assert
        mockMvc.perform(put("/api/v1/locations/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationDto))
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Location not found with ID: 999")));
    }

    @Test
    @DisplayName("Успешное удаление местоположения")
    void shouldDeleteLocationSuccessfully() throws Exception {
        // Arrange
        Location location = new Location(null, "Russia", "Saint Petersburg", "SPB", "Nevsky Prospect, 1");
        location = locationRepository.save(location);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/locations/" + location.getId())
                .header("Authorization", token))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего местоположения")
    void shouldFailToDeleteNonExistentLocation() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/locations/999")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("Location not found with ID: 999")));
    }
}

