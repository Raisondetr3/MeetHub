package ru.itmo.cs.service.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.auth.UserDto;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.participant.ParticipantDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.service.EventService;
import ru.itmo.cs.service.LocationService;
import ru.itmo.cs.service.ParticipantService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantFacade {

    private final ParticipantService participantService;
    private final UserService userService;
    private final EventService eventService;
    private final LocationService locationService;
    private final EntityMapper entityMapper;

    /**
     * Регистрирует участника мероприятия.
     *
     * @param participantDto DTO участника
     * @param eventId        ID мероприятия
     * @return зарегистрированный участник
     */
    public ParticipantDto registerParticipant(ParticipantDto participantDto, Integer eventId) {
        UserDto user = userService.findById(participantDto.getUserId());
        EventDto event = eventService.getEventById(eventId);

        Location location = locationService.getLocationById(event.getVenue().getLocation().getId());
        Venue venue = entityMapper.toVenueEntity(event.getVenue(), location);

        Category category = entityMapper.toCategoryEntity(event.getCategory());
        List<Food> food = event.getFood().stream()
            .map(entityMapper::toFoodEntity)
            .toList();

        Participant participant = entityMapper.toParticipantEntity(
            participantDto,
            entityMapper.toEventEntity(event, venue, category, food),
            entityMapper.toUserEntity(user)
        );
        Participant registeredParticipant = participantService.registerParticipant(participant);

        return entityMapper.toParticipantDto(registeredParticipant);
    }

    /**
     * Регистрирует пользователя как организатора мероприятия.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     * @return зарегистрированный организатор
     */
    public ParticipantDto registerOrganizer(Integer userId, Integer eventId) {
        UserDto user = userService.findById(userId);
        EventDto event = eventService.getEventById(eventId);

        Location location = locationService.getLocationById(event.getVenue().getLocation().getId());
        Venue venue = entityMapper.toVenueEntity(event.getVenue(), location);

        Category category = entityMapper.toCategoryEntity(event.getCategory());
        List<Food> food = event.getFood().stream()
            .map(entityMapper::toFoodEntity)
            .toList();

        Participant organizer = participantService.registerOrganizer(
            entityMapper.toUserEntity(user),
            entityMapper.toEventEntity(event, venue, category, food)
        );
        return entityMapper.toParticipantDto(organizer);
    }

    /**
     * Получает список участников мероприятия.
     *
     * @param eventId ID мероприятия
     * @return список участников в виде DTO
     */
    @Transactional(readOnly = true)
    public List<ParticipantDto> getParticipantsByEvent(Integer eventId) {
        List<Participant> participants = participantService.getParticipantsByEvent(eventId);
        return participants.stream()
            .map(entityMapper::toParticipantDto)
            .toList();
    }

    /**
     * Удаляет участника из мероприятия.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     */
    public void removeParticipant(Integer userId, Integer eventId) {
        participantService.deleteParticipant(userId, eventId);
    }
}
