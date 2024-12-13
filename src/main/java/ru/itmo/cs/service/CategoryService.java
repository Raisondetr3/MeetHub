package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.Category;
import ru.itmo.cs.entity.CategoryEnum;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.CategoryRepository;

/**
 * Сервис для управления категориями мероприятий.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Возвращает категорию по её названию.
     *
     * @param name название категории
     * @return объект категории
     * @throws ResourceNotFoundException если категория не найдена
     */
    public Category getCategoryByName(CategoryEnum name) {
        return categoryRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}