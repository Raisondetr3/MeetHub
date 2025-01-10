package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.entity.Location;
import ru.itmo.cs.entity.Venue;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.VenueRepository;
import ru.itmo.cs.service.VenueService;
import ru.itmo.cs.util.EntityMapper;

/**
 * Сервис для управления местами проведения мероприятий.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;
    private final LocationService locationService;

    /**
     * Создаёт новое место проведения мероприятия.
     *
     * @param venue объект места проведения
     * @return созданное место проведения
     */
    public Venue createVenue(Venue venue) {
        validateLocation(venue.getLocation().getId());
        return venueRepository.save(venue);
    }

    /**
     * Возвращает место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     * @return объект места проведения
     * @throws ResourceNotFoundException если место проведения не найдено
     */
    @Transactional(readOnly = true)
    public Venue getVenueById(Integer id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
    }

    /**
     * Обновляет данные места проведения мероприятия.
     *
     * @param id          идентификатор места проведения
     * @param venueDetails объект с новыми данными места проведения
     * @return обновлённое место проведения
     * @throws ResourceNotFoundException если место проведения или местоположение не найдено
     */
    public Venue updateVenue(Integer id, Venue venueDetails) {
        Venue venue = getVenueById(id);
        validateLocation(venueDetails.getLocation().getId());

        venue.setName(venueDetails.getName());
        venue.setLocation(venueDetails.getLocation());
        venue.setCapacity(venueDetails.getCapacity());

        return venueRepository.save(venue);
    }

    /**
     * Удаляет место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     * @throws ResourceNotFoundException если место проведения не найдено
     */
    public void deleteVenue(Integer id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue not found");
        }
        venueRepository.deleteById(id);
    }

    /**
     * Проверяет существование местоположения по его ID.
     *
     * @param locationId идентификатор местоположения
     * @throws ResourceNotFoundException если местоположение не найдено
     */
    private void validateLocation(Integer locationId) {
        locationService.getLocationById(locationId);
    }
}

