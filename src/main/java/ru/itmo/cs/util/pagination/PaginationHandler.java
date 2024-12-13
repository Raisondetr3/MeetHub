package ru.itmo.cs.util.pagination;

import org.springframework.data.domain.Pageable;

/**
 * Утилита для пагинации.
 */
public interface PaginationHandler {

    /**
     * Метод для пагинации.
     */
    Pageable createPageable(int page, int size, String sortBy, String sortDir);
}