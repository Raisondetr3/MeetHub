package ru.itmo.cs.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itmo.cs.repository.FoodRepository;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@Rollback
class FoodRepositoryTest extends IntegrationTestBase {

    @Autowired
    private FoodRepository foodRepository;

    private Food defaultFood;

    @BeforeEach
    void setUp() {
        foodRepository.deleteAll();
        defaultFood = new Food();
        defaultFood.setName("Vegetarian Sandwich");
        defaultFood.setComposition("Bread, Tomato, Lettuce, Cheese");
    }

    @Test
    @DisplayName("Should save Food correctly")
    void testSaveFood() {
        Food savedFood = foodRepository.save(defaultFood);

        assertThat(savedFood).isNotNull();
        assertThat(savedFood.getId()).isNotNull();
        assertThat(savedFood.getName()).isEqualTo("Vegetarian Sandwich");
        assertThat(savedFood.getComposition()).isEqualTo("Bread, Tomato, Lettuce, Cheese");
    }

    @Test
    @DisplayName("Should find Food by Name (case-insensitive)")
    void testFindFoodByName() {
        foodRepository.save(defaultFood);

        List<Food> foundFoods = foodRepository.findByNameContainingIgnoreCase("sandwich");

        assertThat(foundFoods).isNotEmpty();
        assertThat(foundFoods).anyMatch(food -> "Vegetarian Sandwich".equals(food.getName()));
    }

    @Test
    @DisplayName("Should not find non-existent Food by Name")
    void testFindNonExistentFoodByName() {
        List<Food> foundFoods = foodRepository.findByNameContainingIgnoreCase("Pizza");

        assertThat(foundFoods).isEmpty();
    }

    @Test
    @DisplayName("Should not save Food without Name")
    void testSaveFoodWithoutName() {
        defaultFood.setName(null);

        assertThatThrownBy(() -> foodRepository.saveAndFlush(defaultFood))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save duplicate Food with the same Name")
    void testSaveDuplicateFood() {
        foodRepository.save(defaultFood);

        Food duplicateFood = new Food();
        duplicateFood.setName("Vegetarian Sandwich");
        duplicateFood.setComposition("Cheese, Tomato, Basil");

        Food savedDuplicateFood = foodRepository.saveAndFlush(duplicateFood);

        assertThat(savedDuplicateFood).isNotNull();
        assertThat(savedDuplicateFood.getId()).isNotNull();
        assertThat(savedDuplicateFood.getName()).isEqualTo("Vegetarian Sandwich");
        assertThat(savedDuplicateFood.getComposition()).isEqualTo("Cheese, Tomato, Basil");
    }

    @Test
    @DisplayName("Should save and retrieve Food with Composition")
    void testSaveFoodWithComposition() {
        defaultFood.setComposition("Cheese, Tomato, Basil");

        Food savedFood = foodRepository.save(defaultFood);

        assertThat(savedFood).isNotNull();
        assertThat(savedFood.getId()).isNotNull();
        assertThat(savedFood.getComposition()).isEqualTo("Cheese, Tomato, Basil");
    }
}
