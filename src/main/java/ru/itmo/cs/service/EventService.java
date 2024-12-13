package ru.itmo.cs.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.event.EventFilterCriteria;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.dto.notification.NotificationDto;
import ru.itmo.cs.dto.participant.ParticipantDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.exception.UnauthorizedException;
import ru.itmo.cs.exception.ValidationException;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.util.EntityMapper;
import ru.itmo.cs.util.filter.FilterProcessor;
import ru.itmo.cs.util.pagination.PaginationHandler;

/**
 * Сервис для работы с мероприятиями.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EntityMapper entityMapper;
    private final FilterProcessor<EventDto, EventFilterCriteria> eventFilterProcessor;
    private final PaginationHandler paginationHandler;
    private final ParticipantService participantService;
    private final NotificationService notificationService;
    private final CategoryService categoryService;
    private final VenueService venueService;
    private final FoodService foodService;

    /**
     * Возвращает мероприятия с фильтрацией и пагинацией.
     *
     * @param name       название мероприятия
     * @param category   категория мероприятия
     * @param city       город проведения
     * @param dateFrom   дата начала фильтрации
     * @param dateTo     дата окончания фильтрации
     * @param page       номер страницы
     * @param size       размер страницы
     * @param sortBy     поле сортировки
     * @param sortDir    направление сортировки (ASC/DESC)
     * @return страница DTO мероприятий
     */
    @Transactional(readOnly = true)
    public Page<EventDto> getFilteredEvents(String name, String category, String city,
                                            LocalDateTime dateFrom, LocalDateTime dateTo,
                                            int page, int size, String sortBy, String sortDir) {
        EventFilterCriteria criteria = new EventFilterCriteria();
        criteria.setName(name);
        criteria.setCategory(category);
        criteria.setCity(city);
        criteria.setDateFrom(dateFrom);
        criteria.setDateTo(dateTo);

        Pageable pageable = paginationHandler.createPageable(page, size, sortBy, sortDir);
        return eventFilterProcessor.filter(criteria, pageable);
    }

    /**
     * Возвращает мероприятие по его ID.
     *
     * @param eventId идентификатор мероприятия
     * @return DTO мероприятия
     */
    public EventDto getEventById(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));
        return entityMapper.toEventDto(event);
    }


    /**
     * Возвращает мероприятие с едой по его ID.
     *
     * @param eventId идентификатор мероприятия
     * @return DTO мероприятия
     */
    public EventDto getEventByIdWithFood(Integer eventId) {
        Event event = eventRepository.findByIdWithFood(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with food not found: " + eventId));
        return entityMapper.toEventDto(event);
    }


    /**
     * Регистрирует пользователя на мероприятие с подтверждением через email.
     *
     * @param eventId ID мероприятия
     * @param user    объект пользователя (подтверждённый из UserService)
     */
    public void registerUserForEvent(Integer eventId, User user) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (participantService.isParticipantExists(user.getId(), eventId)) {
            throw new IllegalStateException("User is already registered for this event");
        }

        Participant participant = new Participant(
            new Participant.ParticipantId(user.getId(), eventId),
            user,
            event,
            false
        );
        participantService.registerParticipant(participant);

        String content = "Вы успешно зарегистрировались на мероприятие: " + event.getName();
        NotificationDto notificationDto = new NotificationDto(
            null,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            eventId,
            event.getName(),
            content,
            "SENT",
            new Date()
        );

        notificationService.createNotification(notificationDto, user, event);

    }

    /**
     * Отправляет напоминания участникам мероприятия за 24 часа до его начала.
     *
     * @param eventId ID мероприятия
     */
    public void sendRemindersForEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        notificationService.sendReminderToParticipants(event);
    }

    /**
     * Создаёт новое мероприятие.
     *
     * @param eventDto DTO с данными мероприятия
     * @param user объект пользователя, создавшего мероприятие
     * @return DTO созданного мероприятия
     * @throws ValidationException если входные данные не валидны
     * @throws UnauthorizedException если пользователь не авторизован
     */
    @Transactional
    public EventDto createEvent(EventDto eventDto, User user) {
        if (user == null) {
            throw new UnauthorizedException("Пользователь не авторизован.");
        }

        if (eventDto.getName().length() > 100) {
            throw new ValidationException("Название мероприятия не должно превышать 100 символов.");
        }
        if (eventDto.getDescription().length() > 500) {
            throw new ValidationException("Описание мероприятия не должно превышать 500 символов.");
        }

        Venue venue = venueService.getVenueById(eventDto.getVenue().getId());
//        if (venue.getCapacity() < 1 || venue.getCapacity() > 500) {
//            throw new ValidationException("Вместимость мероприятия должна быть от 1 до 500.");
//        }

        CategoryEnum categoryEnum = CategoryEnum.valueOf(eventDto.getCategory().getName().toUpperCase());
        Category category = categoryService.getCategoryByName(categoryEnum);

        List<Food> food = new ArrayList<>();
        if (eventDto.getFood() != null) {
            for (FoodDto foodDto : eventDto.getFood()) {
                if (foodDto.getId() != null) {
                    food.add(foodService.getFoodById(foodDto.getId()));
                } else {
                    food.add(entityMapper.toFoodEntity(foodDto));
                }
            }
        }

        Event event = entityMapper.toEventEntity(eventDto, venue, category, food);
//        event.setUpdatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);

        participantService.registerOrganizer(user, savedEvent);

        return entityMapper.toEventDto(savedEvent);
    }

    /**
     * Обновляет мероприятие.
     *
     * @param eventId  ID мероприятия, которое нужно обновить
     * @param eventDto DTO с новыми данными мероприятия
     * @param user     объект пользователя, выполняющего обновление
     * @return DTO обновленного мероприятия
     * @throws UnauthorizedException если пользователь не является организатором
     * @throws ResourceNotFoundException если мероприятие не найдено
     * @throws ValidationException если входные данные не валидны
     */
    @Transactional
    public EventDto updateEvent(Integer eventId, EventDto eventDto, User user) {
        if (user == null) {
            throw new UnauthorizedException("Пользователь не авторизован.");
        }

        Event event = eventRepository.findByIdWithFood(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено с ID: " + eventId));

        participantService.validateOrganizer(eventId, user.getId());

        if (eventDto.getName() != null && eventDto.getName().length() > 100) {
            throw new ValidationException("Название мероприятия не должно превышать 100 символов.");
        }
        if (eventDto.getDescription() != null && eventDto.getDescription().length() > 500) {
            throw new ValidationException("Описание мероприятия не должно превышать 500 символов.");
        }

        if (eventDto.getName() != null) {
            event.setName(eventDto.getName());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getDate() != null) {
            event.setDate(eventDto.getDate());
        }

        if (eventDto.getVenue() != null) {
            Venue venue = venueService.getVenueById(eventDto.getVenue().getId());
            if (venue.getCapacity() < 1 || venue.getCapacity() > 500) {
                throw new ValidationException("Вместимость мероприятия должна быть от 1 до 500.");
            }
            event.setVenue(venue);
        }

        if (eventDto.getCategory() != null) {
            CategoryEnum categoryEnum = CategoryEnum.valueOf(eventDto.getCategory().getName().toUpperCase());
            Category category = categoryService.getCategoryByName(categoryEnum);
            event.setCategory(category);
        }

        if (eventDto.getFood() != null) {
            List<Food> food = new ArrayList<>();
            for (FoodDto foodDto : eventDto.getFood()) {
                if (foodDto.getId() != null) {
                    food.add(foodService.getFoodById(foodDto.getId()));
                } else {
                    food.add(entityMapper.toFoodEntity(foodDto));
                }
            }
            event.setFood(food);
        }

        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);

        return entityMapper.toEventDto(updatedEvent);
    }

    /**
     * Возвращает список участников мероприятия.
     *
     * @param eventId ID мероприятия
     * @param user    объект пользователя, выполняющего запрос
     * @return список DTO участников
     * @throws UnauthorizedException если пользователь не является организатором
     * @throws ResourceNotFoundException если мероприятие не найдено
     */
    @Transactional(readOnly = true)
    public List<ParticipantDto> getParticipants(Integer eventId, User user) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));

        participantService.validateOrganizer(eventId, user.getId());

        List<Participant> participants = participantService.getParticipantsByEvent(eventId);
        return participants.stream()
            .map(entityMapper::toParticipantDto)
            .toList();
    }

    /**
     * Удаляет участника из мероприятия.
     *
     * @param eventId ID мероприятия
     * @param participantId ID участника, которого нужно удалить
     * @param user    объект пользователя, выполняющего запрос
     * @throws UnauthorizedException если пользователь не является организатором
     * @throws ResourceNotFoundException если мероприятие или участник не найдены
     */
    @Transactional
    public void removeParticipant(Integer eventId, Integer participantId, User user) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));

        participantService.validateOrganizer(eventId, user.getId());

        if (!participantService.isParticipantExists(participantId, eventId)) {
            throw new ResourceNotFoundException("Участник не найден");
        }

        participantService.deleteParticipant(participantId, eventId);
    }

    /**
     * Удаляет мероприятие. Доступно только для организатора.
     *
     * @param eventId ID мероприятия
     * @param user объект пользователя, выполняющего запрос
     * @throws UnauthorizedException если пользователь не является организатором
     * @throws ResourceNotFoundException если мероприятие не найдено
     */
    @Transactional
    public void deleteEvent(Integer eventId, User user) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено"));

        participantService.validateOrganizer(eventId, user.getId());

        eventRepository.delete(event);
    }
}