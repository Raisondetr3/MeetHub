package ru.itmo.cs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.cs.entity.Location;

/**
 * Репозиторий местоположения для обращения к БД.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
  /**
   * Находит местоположение по стране.
   *
   * @param country название страны
   *
   * @return местоположение
   */
  List<Location> findByCountry(String country);

  /**
   * Находит местоположение по городу.
   *
   * @param city название города
   *
   * @return местоположение
   */
  List<Location> findByCity(String city);
}
