package ru.itmo.cs.unit.service;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.entity.Food;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.repository.FoodRepository;
import ru.itmo.cs.service.FoodService;
import ru.itmo.cs.service.ParticipantService;

class FoodServiceTest {

    @InjectMocks
    private FoodService foodService;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ParticipantService participantService;

    private User testUser;
    private Food testFood;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1);

        testFood = new Food();
        testFood.setId(1);
        testFood.setName("Pizza");
        testFood.setComposition("Cheese, Tomato, Dough");
    }

    @Test
    void createFoodList_ShouldCreateFoodListSuccessfully() {
        // Arrange
        List<FoodDto> foodDtos = List.of(
            new FoodDto(null, "Pizza", "Cheese, Tomato, Dough"),
            new FoodDto(null, "Pasta", "Flour, Eggs, Olive Oil")
        );

        List<Food> foodEntities = List.of(
            new Food("Pizza", "Cheese, Tomato, Dough"),
            new Food("Pasta", "Flour, Eggs, Olive Oil")
        );

        when(entityMapper.toFoodEntity(any(FoodDto.class))).thenAnswer(invocation -> {
            FoodDto dto = invocation.getArgument(0);
            return new Food(dto.getName(), dto.getComposition());
        });
        when(foodRepository.saveAll(anyList())).thenReturn(foodEntities);

        // Act
        List<Food> result = foodService.createFoodList(foodDtos, testUser, 1);

        // Assert
        assertEquals(2, result.size());
        verify(participantService).validateOrganizer(1, testUser.getId());
        verify(foodRepository).saveAll(anyList());
    }

    @Test
    void createFood_ShouldCreateFoodSuccessfully() {
        // Arrange
        FoodDto foodDto = new FoodDto(null, "Pizza", "Cheese, Tomato, Dough");

        when(entityMapper.toFoodEntity(foodDto)).thenReturn(testFood);
        when(foodRepository.save(testFood)).thenReturn(testFood);
        when(entityMapper.toFoodDto(testFood)).thenReturn(foodDto);

        // Act
        FoodDto result = foodService.createFood(foodDto, testUser, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Pizza", result.getName());
        verify(participantService).validateOrganizer(1, testUser.getId());
        verify(foodRepository).save(testFood);
    }

    @Test
    void getAllFood_ShouldReturnAllFoodSuccessfully() {
        // Arrange
        List<Food> foods = List.of(testFood);
        when(foodRepository.findAll()).thenReturn(foods);
        when(entityMapper.toFoodDto(any(Food.class))).thenReturn(new FoodDto(1, "Pizza", "Cheese, Tomato, Dough"));

        // Act
        List<FoodDto> result = foodService.getAllFood();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
        verify(foodRepository).findAll();
    }

    @Test
    void getFoodById_ShouldReturnFoodSuccessfully() {
        // Arrange
        when(foodRepository.findById(1)).thenReturn(Optional.of(testFood));

        // Act
        Food result = foodService.getFoodById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Pizza", result.getName());
        verify(foodRepository).findById(1);
    }

    @Test
    void getFoodById_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(foodRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> foodService.getFoodById(1)
        );
        assertEquals("Food not found with ID: 1", exception.getMessage());
        verify(foodRepository).findById(1);
    }

    @Test
    void updateFood_ShouldUpdateFoodSuccessfully() {
        // Arrange
        FoodDto foodDto = new FoodDto(null, "Pizza Updated", "Cheese, Tomato, Basil");

        when(foodRepository.findById(1)).thenReturn(Optional.of(testFood));
        when(foodRepository.save(any(Food.class))).thenReturn(testFood);
        when(entityMapper.toFoodDto(testFood)).thenReturn(foodDto);

        // Act
        FoodDto result = foodService.updateFood(1, foodDto, testUser, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Pizza Updated", result.getName());
        verify(participantService).validateOrganizer(1, testUser.getId());
        verify(foodRepository).save(testFood);
    }

    @Test
    void deleteFood_ShouldDeleteFoodSuccessfully() {
        // Arrange
        when(foodRepository.findById(1)).thenReturn(Optional.of(testFood));

        // Act
        foodService.deleteFood(1, testUser, 1);

        // Assert
        verify(participantService).validateOrganizer(1, testUser.getId());
        verify(foodRepository).delete(testFood);
    }

    @Test
    void searchFoodByName_ShouldReturnFoodSuccessfully() {
        // Arrange
        List<Food> foods = List.of(testFood);
        when(foodRepository.findByNameContainingIgnoreCase("Pizza")).thenReturn(foods);
        when(entityMapper.toFoodDto(any(Food.class))).thenReturn(new FoodDto(1, "Pizza", "Cheese, Tomato, Dough"));

        // Act
        List<FoodDto> result = foodService.searchFoodByName("Pizza");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
        verify(foodRepository).findByNameContainingIgnoreCase("Pizza");
    }
}