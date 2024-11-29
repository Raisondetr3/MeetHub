package ru.itmo.cs.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Category;
import ru.itmo.cs.entity.CategoryEnum;

/**
 * Репозиторий категории для обращения к БД.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
  /**
   * Находит категорию по названию.
   *
   * @param name название
   *
   * @return категория
   */
  Optional<Category> findByName(CategoryEnum name);
}

