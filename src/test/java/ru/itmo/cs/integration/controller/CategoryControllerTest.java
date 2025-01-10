package ru.itmo.cs.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User defaultUser;
    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя
        defaultUser = new User();
        defaultUser.setUsername("testUser");
        defaultUser.setEmail("test@example.com");
        defaultUser.setPassword("password");
        defaultUser = userRepository.save(defaultUser);

        // Создание тестовой категории
        defaultCategory = new Category();
        defaultCategory.setName(CategoryEnum.CONFERENCE);
        defaultCategory = categoryRepository.save(defaultCategory);
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешное получение категории по названию")
    void shouldGetCategoryByNameSuccessfully() throws Exception {
        // Arrange
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/CONFERENCE")
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(defaultCategory.getId()))
            .andExpect(jsonPath("$.name").value(defaultCategory.getName().toString()));
    }

    @Test
    @DisplayName("Ошибка при попытке получить несуществующую категорию")
    void shouldFailToGetNonExistentCategory() throws Exception {
        // Arrange
        String token = generateToken(defaultUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/WORKSHOP")
                .header("Authorization", token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Category not found"));
    }

    protected String generateToken(User user) {
        // Здесь должна быть логика генерации JWT токена для тестового пользователя
        return "Bearer your-generated-token";
    }
}
