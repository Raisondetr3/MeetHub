package ru.itmo.cs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.category.CategoryDto;
import ru.itmo.cs.entity.Category;
import ru.itmo.cs.entity.CategoryEnum;
import ru.itmo.cs.service.CategoryService;
import ru.itmo.cs.util.EntityMapper;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "API для управления категориями мероприятий")
public class CategoryController {

    private final CategoryService categoryService;
    private final EntityMapper entityMapper;

    /**
     * Возвращает категорию по её названию.
     *
     * @param name название категории
     * @return объект категории
     */
    @GetMapping("/{name}")
    @Operation(summary = "Возвращает категорию по её названию")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Категория успешно найдена"),
        @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<CategoryDto> getCategoryByName(@PathVariable String name) {
        Category category = categoryService.getCategoryByName(CategoryEnum.valueOf(name.toUpperCase()));
        return ResponseEntity.ok(entityMapper.toCategoryDto(category));
    }
}