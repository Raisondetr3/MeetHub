package ru.itmo.cs.service.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.entity.Location;
import ru.itmo.cs.entity.Venue;
import ru.itmo.cs.service.LocationService;
import ru.itmo.cs.service.VenueService;
import ru.itmo.cs.util.EntityMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class VenueFacade {

    private final VenueService venueService;
    private final LocationService locationService;
    private final EntityMapper entityMapper;

    /**
     * Создаёт новое место проведения мероприятия.
     *
     * @param venueDto DTO места проведения
     * @return созданное место проведения в виде DTO
     */
    public VenueDto createVenue(VenueDto venueDto) {
        Location location = locationService.getLocationById(venueDto.getLocation().getId());
        Venue venue = entityMapper.toVenueEntity(venueDto, location);
        return entityMapper.toVenueDto(venueService.createVenue(venue));
    }

    /**
     * Возвращает место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     * @return DTO места проведения
     */
    @Transactional(readOnly = true)
    public VenueDto getVenueById(Integer id) {
        Venue venue = venueService.getVenueById(id);
        return entityMapper.toVenueDto(venue);
    }

    /**
     * Обновляет место проведения мероприятия.
     *
     * @param id идентификатор места проведения
     * @param venueDto объект DTO места проведения
     * @return обновленное место проведения в виде DTO
     */
    public VenueDto updateVenue(Integer id, VenueDto venueDto) {
        Location location = locationService.getLocationById(venueDto.getLocation().getId());
        Venue venueDetails = entityMapper.toVenueEntity(venueDto, location);
        Venue updatedVenue = venueService.updateVenue(id, venueDetails);
        return entityMapper.toVenueDto(updatedVenue);
    }

    /**
     * Удаляет место проведения по его идентификатору.
     *
     * @param id идентификатор места проведения
     */
    public void deleteVenue(Integer id) {
        venueService.deleteVenue(id);
    }
}
