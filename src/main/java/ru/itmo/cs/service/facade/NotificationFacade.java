package ru.itmo.cs.service.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.service.EventService;
import ru.itmo.cs.service.LocationService;
import ru.itmo.cs.service.ParticipantService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationFacade {

    private final EventService eventService;
    private final ParticipantService participantService;
    private final LocationService locationService;
    private final EntityMapper entityMapper;

    /**
     * Получает организатора мероприятия по ID.
     *
     * @param eventId идентификатор мероприятия
     * @return DTO организатора мероприятия
     * @throws ResourceNotFoundException если мероприятие или организатор не найдены
     */
    public User getEventOrganizer(Integer eventId) {
        EventDto eventDto = eventService.getEventById(eventId);

        Location location = locationService.getLocationById(eventDto.getVenue().getLocation().getId());
        Venue venue = entityMapper.toVenueEntity(eventDto.getVenue(), location);

        Category category = entityMapper.toCategoryEntity(eventDto.getCategory());
        List<Food> food = eventDto.getFood().stream()
            .map(entityMapper::toFoodEntity)
            .toList();

        Event event = entityMapper.toEventEntity(eventDto, venue, category, food);

        Participant organizer = participantService.findEventCreator(event)
            .orElseThrow(() -> new ResourceNotFoundException("Организатор мероприятия не найден"));

        return organizer.getUser();
    }

    /**
     * Получает сущность мероприятия по ID.
     *
     * @param eventId идентификатор мероприятия
     * @return сущность Event
     * @throws ResourceNotFoundException если мероприятие не найдено
     */
    public Event getEventEntityById(Integer eventId) {
        EventDto eventDto = eventService.getEventById(eventId);

        Location location = locationService.getLocationById(eventDto.getVenue().getLocation().getId());
        Venue venue = entityMapper.toVenueEntity(eventDto.getVenue(), location);

        Category category = entityMapper.toCategoryEntity(eventDto.getCategory());
        List<Food> food = eventDto.getFood().stream()
            .map(entityMapper::toFoodEntity)
            .toList();

        return entityMapper.toEventEntity(eventDto, venue, category, food);
    }
}