package ru.itmo.cs.dto.pagination;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для представления пагинации.
 */
@Data
@AllArgsConstructor
public class PaginationResponseDto<T> {
    private List<T> content;
    private int page;
    private long totalElements;
    private int totalPages;
}
