package ru.itmo.cs.util;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.auth.UserCreateDto;
import ru.itmo.cs.dto.auth.UserDto;
import ru.itmo.cs.dto.category.CategoryDto;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.food.FoodDto;
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
            venue.getLocation().getAddress()
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
     * Преобразует DTO для создания пользователя в сущность User.
     *
     * @param dto DTO для создания пользователя
     * @param passwordEncoder инстанс PasswordEncoder
     * @return сущность User
     */
    public User toUserEntity(UserCreateDto dto, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
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
}
