package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.Venue;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.VenueRepository;

/**
 * Сервис для управления местами проведения мероприятий.
 */
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    /**
     * Создаёт новое место проведения мероприятия.
     *
     * @param venue объект места проведения
     * @return созданное место проведения
     */
    @Transactional
    public Venue createVenue(Venue venue) {
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
     * @param id идентификатор места проведения
     * @param venueDetails объект с новыми данными места проведения
     * @return обновлённое место проведения
     * @throws ResourceNotFoundException если место проведения не найдено
     */
    @Transactional
    public Venue updateVenue(Integer id, Venue venueDetails) {
        Venue venue = getVenueById(id);
        venue.setName(venueDetails.getName());
        venue.setLocation(venueDetails.getLocation());
        venue.setCapacity(venueDetails.getCapacity());
        return venueRepository.save(venue);
    }

    /**
     * Удаляет место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     */
    @Transactional
    public void deleteVenue(Integer id) {
        venueRepository.deleteById(id);
    }
}

