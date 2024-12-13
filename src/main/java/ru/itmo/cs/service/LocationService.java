package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.Location;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.LocationRepository;

import java.util.List;

/**
 * Сервис для управления местоположениями.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    /**
     * Создаёт новое местоположение.
     *
     * @param location объект местоположения
     * @return созданное местоположение
     */
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    /**
     * Возвращает местоположение по его идентификатору.
     *
     * @param id идентификатор местоположения
     * @return объект местоположения
     * @throws ResourceNotFoundException если местоположение не найдено
     */
    @Transactional(readOnly = true)
    public Location getLocationById(Integer id) {
        return locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
    }

    /**
     * Возвращает список местоположений по стране.
     *
     * @param country название страны
     * @return список местоположений
     */
    @Transactional(readOnly = true)
    public List<Location> getLocationsByCountry(String country) {
        return locationRepository.findByCountry(country);
    }

    /**
     * Возвращает список местоположений по городу.
     *
     * @param city название города
     * @return список местоположений
     */
    @Transactional(readOnly = true)
    public List<Location> getLocationsByCity(String city) {
        return locationRepository.findByCity(city);
    }

    /**
     * Обновляет данные местоположения.
     *
     * @param id            идентификатор местоположения
     * @param locationDetails объект с новыми данными местоположения
     * @return обновлённое местоположение
     * @throws ResourceNotFoundException если местоположение не найдено
     */
    public Location updateLocation(Integer id, Location locationDetails) {
        Location location = getLocationById(id);
        location.setCountry(locationDetails.getCountry());
        location.setRegion(locationDetails.getRegion());
        location.setCity(locationDetails.getCity());
        location.setAddress(locationDetails.getAddress());
        return locationRepository.save(location);
    }

    /**
     * Удаляет местоположение по его идентификатору.
     *
     * @param id идентификатор местоположения
     * @throws ResourceNotFoundException если местоположение не найдено
     */
    public void deleteLocation(Integer id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found with ID: " + id);
        }
        locationRepository.deleteById(id);
    }
}