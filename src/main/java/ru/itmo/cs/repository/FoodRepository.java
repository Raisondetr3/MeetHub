package ru.itmo.cs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Food;

/**
 * Репозиторий еды для обращения к БД.
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {
    /**
     * Находит еду по его названию без учёта регистров.
     *
     * @param name название еды
     * @return еда
     */
    List<Food> findByNameContainingIgnoreCase(String name);
}
