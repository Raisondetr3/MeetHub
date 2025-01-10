package ru.itmo.cs.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.ticket.TicketDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Ticket;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.exception.UnauthorizedException;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.repository.TicketRepository;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

/**
 * Сервис для работы с билетами.
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EntityMapper entityMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт новый билет для пользователя на указанное мероприятие.
     *
     * @param ticketDto DTO билета
     * @param user      текущий пользователь
     * @return созданный билет
     * @throws ResourceNotFoundException если мероприятие или пользователь не найдены
     * @throws IllegalStateException     если место уже занято
     */
    @Transactional
    public TicketDto createTicket(TicketDto ticketDto, User user) {
        Event event = eventRepository.findById(ticketDto.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено с ID: " + ticketDto.getEventId()));

        if (ticketRepository.findBySeatNumberAndEvent(ticketDto.getSeatNumber(), event).isPresent()) {
            throw new IllegalStateException("Место уже занято: " + ticketDto.getSeatNumber());
        }

        Ticket ticket = entityMapper.toTicketEntity(ticketDto, event, user);
        Ticket savedTicket = ticketRepository.save(ticket);
        return entityMapper.toTicketDto(savedTicket);
    }

    /**
     * Получает список билетов текущего пользователя.
     *
     * @param user текущий пользователь
     * @return список билетов
     */
    @Transactional(readOnly = true)
    public List<TicketDto> getUserTickets(User user) {
        List<Ticket> tickets = ticketRepository.findByUser(user);
        return tickets.stream()
            .map(entityMapper::toTicketDto)
            .toList();
    }

    /**
     * Получает список билетов на мероприятие.
     *
     * @param eventId идентификатор мероприятия
     * @return список билетов
     * @throws ResourceNotFoundException если мероприятие не найдено
     */
    @Transactional(readOnly = true)
    public List<TicketDto> getEventTickets(Integer eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Мероприятие не найдено с ID: " + eventId));

        List<Ticket> tickets = ticketRepository.findByEvent(event);
        return tickets.stream()
            .map(entityMapper::toTicketDto)
            .toList();
    }

    /**
     * Удаляет билет по идентификатору.
     *
     * @param ticketId идентификатор билета
     * @param user     текущий пользователь
     * @throws ResourceNotFoundException если билет не найден
     * @throws UnauthorizedException     если пользователь не является владельцем билета
     */
    @Transactional
    public void deleteTicket(Integer ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Билет не найден с ID: " + ticketId));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Пользователь не является владельцем билета");
        }

        ticketRepository.delete(ticket);
    }
}