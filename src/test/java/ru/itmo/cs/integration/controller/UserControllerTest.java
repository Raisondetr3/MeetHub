package ru.itmo.cs.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.itmo.cs.dto.auth.*;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.JwtService;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;


    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpMocks() {
        when(jwtService.generateToken(any())).thenReturn("mockedToken");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded" + invocation.getArgument(0));
        when(passwordEncoder.matches(any(), any())).thenAnswer(invocation ->
            invocation.getArgument(0).equals(invocation.getArgument(1).toString().replace("encoded", ""))
        );
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        UserCreateDto userCreateDto = new UserCreateDto("testUser", "test@example.com", "password");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("mockedToken"))
            .andExpect(jsonPath("$.user.username", is("testUser")))
            .andExpect(jsonPath("$.user.email", is("test@example.com")));

        // Verify user exists in the database
        assertTrue(userRepository.findByUsername("testUser").isPresent());
    }

    @Test
    @DisplayName("Ошибка при регистрации пользователя с существующим именем")
    void shouldFailToRegisterUserWithExistingUsername() throws Exception {
        // Arrange
        userRepository.save(new User(null, "testUser", "test@example.com", "encodedPassword"));
        UserCreateDto userCreateDto = new UserCreateDto("testUser", "another@example.com", "password");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", is("Пользователь с таким именем уже существует")));
    }

    @Test
    @DisplayName("Успешный вход в систему")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        userRepository.save(new User(null, "testUser", "test@example.com", passwordEncoder.encode("password")));
        LoginRequestDto loginRequestDto = new LoginRequestDto("testUser", "password");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.username", is("testUser")))
            .andExpect(jsonPath("$.user.email", is("test@example.com")));
    }

    @Test
    @DisplayName("Ошибка при входе с неверными учетными данными")
    void shouldFailToLoginWithInvalidCredentials() throws Exception {
        // Arrange
        userRepository.save(new User(null, "testUser", "test@example.com", passwordEncoder.encode("password")));
        LoginRequestDto loginRequestDto = new LoginRequestDto("testUser", "wrongPassword");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Неверное имя пользователя или пароль")));
    }

    @Test
    @DisplayName("Ошибка при входе с несуществующим именем пользователя")
    void shouldFailToLoginWithNonExistentUsername() throws Exception {
        // Arrange
        LoginRequestDto loginRequestDto = new LoginRequestDto("nonExistentUser", "password");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Неверное имя пользователя или пароль")));
    }
}
