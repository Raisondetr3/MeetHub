package ru.itmo.cs.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.food.FoodDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Food;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.FoodRepository;
import ru.itmo.cs.util.EntityMapper;

/**
 * Сервис для управления едой мероприятий.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FoodService {

    private final FoodRepository foodRepository;
    private final EntityMapper entityMapper;
    private final ParticipantService participantService;

    /**
     * Создает список еды из DTO и сохраняет их в базе.
     *
     * @param foodDtos список DTO еды
     * @return список созданных сущностей еды
     */
    public List<Food> createFoodList(List<FoodDto> foodDtos, User user, Integer eventId) {
        participantService.validateOrganizer(eventId, user.getId());
        List<Food> foodEntities = new ArrayList<>();
        for (FoodDto foodDto : foodDtos) {
            foodEntities.add(entityMapper.toFoodEntity(foodDto));
        }
        return foodRepository.saveAll(foodEntities);
    }

    /**
     * Создает новое блюдо.
     *
     * @param foodDto DTO блюда
     * @return DTO созданного блюда
     */
    public FoodDto createFood(FoodDto foodDto, User user, Integer eventId) {
        participantService.validateOrganizer(eventId, user.getId());
        Food food = entityMapper.toFoodEntity(foodDto);
        Food savedFood = foodRepository.save(food);
        return entityMapper.toFoodDto(savedFood);
    }


    /**
     * Возвращает список всех блюд.
     *
     * @return список DTO всех блюд
     */
    @Transactional(readOnly = true)
    public List<FoodDto> getAllFood() {
        return foodRepository.findAll()
            .stream()
            .map(entityMapper::toFoodDto)
            .toList();
    }

    /**
     * Возвращает блюдо по его ID.
     *
     * @param id идентификатор блюда
     * @return объект сущности Food
     * @throws ResourceNotFoundException если блюдо не найдено
     */
    @Transactional(readOnly = true)
    public Food getFoodById(Integer id) {
        return foodRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Food not found with ID: " + id));
    }

    /**
     * Обновляет данные блюда.
     *
     * @param id идентификатор блюда
     * @param foodDto DTO с обновленными данными блюда
     * @return обновленный DTO блюда
     * @throws ResourceNotFoundException если блюдо не найдено
     */
    public FoodDto updateFood(Integer id, FoodDto foodDto, User user, Integer eventId) {
        participantService.validateOrganizer(eventId, user.getId());

        Food existingFood = foodRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Food not found with ID: " + id));

        existingFood.setName(foodDto.getName());
        existingFood.setComposition(foodDto.getComposition());

        Food updatedFood = foodRepository.save(existingFood);
        return entityMapper.toFoodDto(updatedFood);
    }

    /**
     * Удаляет блюдо по его ID.
     *
     * @param id идентификатор блюда
     * @throws ResourceNotFoundException если блюдо не найдено
     */
    public void deleteFood(Integer id, User user, Integer eventId) {
        participantService.validateOrganizer(eventId, user.getId());

        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with ID: " + id));

        for (Event event : food.getEvents()) {
            event.getFood().remove(food);
        }

        foodRepository.delete(food);
    }

    /**
     * Возвращает список блюд, название которых содержит указанный текст.
     *
     * @param name часть названия блюда
     * @return список DTO блюд
     */
    @Transactional(readOnly = true)
    public List<FoodDto> searchFoodByName(String name) {
        return foodRepository.findByNameContainingIgnoreCase(name)
            .stream()
            .map(entityMapper::toFoodDto)
            .toList();
    }
}