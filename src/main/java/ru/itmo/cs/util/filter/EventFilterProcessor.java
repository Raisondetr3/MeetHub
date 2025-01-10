package ru.itmo.cs.util.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.event.EventFilterCriteria;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.util.EntityMapper;

@Component
@RequiredArgsConstructor
public class EventFilterProcessor implements FilterProcessor<EventDto, EventFilterCriteria> {

    private final EventRepository eventRepository;
    private final EntityMapper entityMapper;

    /**
     * Фильтрует мероприятия по заданным критериям.
     *
     * @param criteria объект с критериями фильтрации
     * @param pageable объект пагинации и сортировки
     * @return страница DTO мероприятий
     */
    @Override
    public Page<EventDto> filter(EventFilterCriteria criteria, Pageable pageable) {
        return eventRepository.findByFilters(
            criteria.getName(),
            criteria.getCategory(),
            criteria.getCity(),
            criteria.getDateFrom(),
            criteria.getDateTo(),
            pageable
        ).map(entityMapper::toEventDto);
    }
}
