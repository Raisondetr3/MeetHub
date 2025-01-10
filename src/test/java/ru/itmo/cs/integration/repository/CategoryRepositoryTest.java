package ru.itmo.cs.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.Category;
import ru.itmo.cs.entity.CategoryEnum;
import ru.itmo.cs.integration.IntegrationTestBase;
import ru.itmo.cs.repository.CategoryRepository;


@Rollback
@DataJpaTest
class CategoryRepositoryTest extends IntegrationTestBase {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        defaultCategory = new Category();
        defaultCategory.setName(CategoryEnum.SEMINAR);
    }

    @Test
    @DisplayName("Should save Category correctly")
    void testSaveCategory() {
        Category savedCategory = categoryRepository.save(defaultCategory);

        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo(CategoryEnum.SEMINAR);
    }

    @Test
    @DisplayName("Should find Category by Name")
    void testFindCategoryByName() {
        categoryRepository.save(defaultCategory);

        Optional<Category> foundCategory = categoryRepository.findByName(CategoryEnum.SEMINAR);

        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo(CategoryEnum.SEMINAR);
    }

    @Test
    @DisplayName("Should not find non-existent Category")
    void testFindNonExistentCategory() {
        Optional<Category> foundCategory = categoryRepository.findByName(CategoryEnum.CONFERENCE);

        assertThat(foundCategory).isNotPresent();
    }

    @Test
    @DisplayName("Should not save duplicate Category")
    void testSaveDuplicateCategory() {
        categoryRepository.save(defaultCategory);

        Category duplicateCategory = new Category();
        duplicateCategory.setName(CategoryEnum.SEMINAR);

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicateCategory))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not save Category without Name")
    void testSaveCategoryWithoutName() {
        defaultCategory.setName(null);

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(defaultCategory))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
