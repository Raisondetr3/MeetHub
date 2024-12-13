package ru.itmo.cs.util;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.auth.UserCreateDto;
import ru.itmo.cs.dto.auth.UserDto;
import ru.itmo.cs.dto.category.CategoryDto;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.dto.location.LocationDto;
import ru.itmo.cs.dto.notification.NotificationDto;
import ru.itmo.cs.dto.participant.ParticipantDto;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.entity.*;

/**
 * Утилита для преобразования между сущностями и DTO.
 */
@Component
public class EntityMapper {

    /**
     * Преобразует сущность User в DTO.
     *
     * @param user сущность User
     * @return UserDto
     */
    public UserDto toUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }

    /**
     * Преобразует сущность Event в EventDto.
     *
     * @param event сущность Event
     * @return EventDto
     */
    public EventDto toEventDto(Event event) {
        return new EventDto(
            event.getId(),
            event.getName(),
            event.getDescription(),
            event.getDate(),
            toVenueDto(event.getVenue()),
            toCategoryDto(event.getCategory()),
            event.getFood().stream().map(this::toFoodDto).toList()
        );
    }

    /**
     * Преобразует сущность Venue в VenueDto.
     *
     * @param venue сущность Venue
     * @return VenueDto
     */
    public VenueDto toVenueDto(Venue venue) {
        return new VenueDto(
            venue.getId(),
            venue.getName(),
            venue.getCapacity(),
            toLocationDto(venue.getLocation())
        );
    }

    /**
     * Преобразует сущность Location в DTO LocationDto.
     *
     * @param location объект Location
     * @return объект LocationDto
     */
    public LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getCountry(),
                location.getRegion(),
                location.getCity(),
                location.getAddress()
        );
    }

    /**
     * Преобразует сущность Category в CategoryDto.
     *
     * @param category сущность Category
     * @return CategoryDto
     */
    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
            category.getId(),
            category.getName().toString()
        );
    }

    /**
     * Преобразует сущность Food в FoodDto.
     *
     * @param food сущность Food
     * @return DTO блюда
     */
    public FoodDto toFoodDto(Food food) {
        return new FoodDto(
            food.getId(),
            food.getName(),
            food.getComposition()
        );
    }

    /**
     * Преобразует Participant в ParticipantDto.
     *
     * @param participant объект Participant
     * @return DTO участника
     */
    public ParticipantDto toParticipantDto(Participant participant) {
        return new ParticipantDto(
            participant.getUser().getId(),
            participant.getUser().getUsername(),
            participant.getUser().getEmail(),
            participant.getIsCreator()
        );
    }

    /**
     * Преобразует Notification в NotificationDto.
     *
     * @param notification объект Notification
     * @return объект NotificationDto
     */
    public NotificationDto toNotificationDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getUser().getId(),
                notification.getUser().getUsername(),
                notification.getUser().getEmail(),
                notification.getEvent().getId(),
                notification.getEvent().getName(),
                notification.getContent(),
                notification.getStatus(),
                notification.getSentAt()
        );
    }

    /**
     * Преобразует DTO для создания пользователя в сущность User.
     *
     * @param dto DTO для создания пользователя
     * @param passwordEncoder инстанс PasswordEncoder
     * @return сущность User
     */
    public User toUserCreateEntity(UserCreateDto dto, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return user;
    }

    /**
     * Преобразует UserDto в сущность User.
     *
     * @param userDto DTO пользователя
     * @return сущность User
     */
    public User toUserEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        return user;
    }

    /**
     * Преобразует EventDTO в сущность Event.
     *
     * @param dto   EventDTO
     * @param venue сущность Venue
     * @param category сущность Category
     * @param food список еды
     * @return сущность Event
     */
    public Event toEventEntity(EventDto dto, Venue venue, Category category, List<Food> food) {
        Event event = new Event();
        event.setId(dto.getId());
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setVenue(venue);
        event.setCategory(category);
        event.setFood(food);
        return event;
    }

    /**
     * Преобразует FoodDto в сущность Food.
     *
     * @param dto DTO блюда
     * @return сущность Food
     */
    public Food toFoodEntity(FoodDto dto) {
        Food food = new Food();
        food.setId(dto.getId());
        food.setName(dto.getName());
        food.setComposition(dto.getComposition());
        return food;
    }

    /**
     * Преобразует DTO Venue в сущность Venue.
     *
     * @param dto      объект VenueDto
     * @param location объект Location
     * @return объект Venue
     */
    public Venue toVenueEntity(VenueDto dto, Location location) {
        Venue venue = new Venue();
        venue.setId(dto.getId());
        venue.setName(dto.getName());
        venue.setCapacity(dto.getCapacity());
        venue.setLocation(location);
        return venue;
    }


    /**
     * Преобразует DTO Location в сущность Location.
     *
     * @param dto объект LocationDto
     * @return объект Location
     */
    public Location toLocationEntity(LocationDto dto) {
        Location location = new Location();
        location.setId(dto.getId());
        location.setCountry(dto.getCountry());
        location.setRegion(dto.getRegion());
        location.setCity(dto.getCity());
        location.setAddress(dto.getAddress());
        return location;
    }

    /**
     * Преобразует DTO Category в сущность Category.
     *
     * @param dto объект CategoryDto
     * @return объект Category
     */
    public Category toCategoryEntity(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(CategoryEnum.valueOf(dto.getName().toUpperCase()));
        return category;
    }

    /**
     * Преобразует ParticipantDto в Participant.
     *
     * @param dto   DTO участника
     * @param event объект мероприятия
     * @param user  объект пользователя
     * @return объект Participant
     */
    public Participant toParticipantEntity(ParticipantDto dto, Event event, User user) {
        return new Participant(
            new Participant.ParticipantId(dto.getUserId(), event.getId()),
            user,
            event,
            dto.getIsCreator()
        );
    }

    /**
     * Преобразует NotificationDto в Notification.
     *
     * @param dto объект NotificationDto
     * @param user объект User
     * @param event объект Event
     * @return объект Notification
     */
    public Notification toNotificationEntity(NotificationDto dto, User user, Event event) {
        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setUser(user);
        notification.setEvent(event);
        notification.setContent(dto.getContent());
        notification.setStatus(dto.getStatus());
        notification.setSentAt(dto.getSentAt());
        return notification;
    }
}
