package ru.itmo.cs.unit.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.CategoryRepository;
import ru.itmo.cs.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName(CategoryEnum.CONFERENCE);
    }

    @Test
    @DisplayName("Успешное получение категории по названию")
    void shouldGetCategoryByNameSuccessfully() {
        // Arrange
        when(categoryRepository.findByName(CategoryEnum.CONFERENCE)).thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.getCategoryByName(CategoryEnum.CONFERENCE);

        // Assert
        assertNotNull(result);
        assertEquals(CategoryEnum.CONFERENCE, result.getName());
        verify(categoryRepository, times(1)).findByName(CategoryEnum.CONFERENCE);
    }

    @Test
    @DisplayName("Ошибка при попытке получить категорию, которая не существует")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findByName(CategoryEnum.WORKSHOP)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.getCategoryByName(CategoryEnum.WORKSHOP),
            "Expected an exception when the category is not found"
        );

        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository, times(1)).findByName(CategoryEnum.WORKSHOP);
    }
}

