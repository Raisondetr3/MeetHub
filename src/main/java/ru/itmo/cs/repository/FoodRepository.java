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
     * Находит блюда по их названию (без учета регистра).
     *
     * @param name название блюда
     * @return список блюд
     */
    List<Food> findByNameContainingIgnoreCase(String name);

    /**
     * Проверяет существование блюда по названию (без учета регистра).
     *
     * @param name название блюда
     * @return true, если блюдо существует
     */
    boolean existsByNameIgnoreCase(String name);
}
