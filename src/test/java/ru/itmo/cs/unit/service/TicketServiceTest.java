package ru.itmo.cs.unit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ru.itmo.cs.dto.ticket.TicketDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Ticket;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.exception.UnauthorizedException;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.repository.TicketRepository;
import ru.itmo.cs.service.TicketService;
import ru.itmo.cs.util.EntityMapper;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EntityMapper entityMapper;

    private User user;
    private Event event;
    private Ticket ticket;
    private TicketDto ticketDto;

    @BeforeEach
    void setUp() {
        user = new User(1, "testUser", "test@example.com", "password");
        event = new Event(1, "Test Event", "Description", LocalDateTime.now().plusDays(1), null, null, List.of(), LocalDateTime.now());
        ticket = new Ticket(1, "A1", event, user);
        ticketDto = new TicketDto(1, "A1", event.getId(), user.getId());
    }

    @Test
    @DisplayName("Успешное создание билета")
    void shouldCreateTicketSuccessfully() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(ticketRepository.findBySeatNumberAndEvent(ticketDto.getSeatNumber(), event)).thenReturn(Optional.empty());
        when(entityMapper.toTicketEntity(ticketDto, event, user)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(entityMapper.toTicketDto(ticket)).thenReturn(ticketDto);

        // Act
        TicketDto result = ticketService.createTicket(ticketDto, user);

        // Assert
        assertNotNull(result);
        assertEquals(ticketDto, result);
        verify(eventRepository).findById(event.getId());
        verify(ticketRepository).findBySeatNumberAndEvent(ticketDto.getSeatNumber(), event);
        verify(ticketRepository).save(ticket);
        verify(entityMapper).toTicketDto(ticket);
    }

    @Test
    @DisplayName("Ошибка при создании билета на занятое место")
    void shouldThrowExceptionIfSeatAlreadyTaken() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(ticketRepository.findBySeatNumberAndEvent(ticketDto.getSeatNumber(), event)).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> ticketService.createTicket(ticketDto, user),
            "Ожидалось исключение при создании билета на занятое место"
        );

        assertEquals("Место уже занято: A1", exception.getMessage());
        verify(eventRepository).findById(event.getId());
        verify(ticketRepository).findBySeatNumberAndEvent(ticketDto.getSeatNumber(), event);
        verifyNoInteractions(entityMapper);
    }

    @Test
    @DisplayName("Успешное получение билетов пользователя")
    void shouldGetUserTicketsSuccessfully() {
        // Arrange
        when(ticketRepository.findByUser(user)).thenReturn(List.of(ticket));
        when(entityMapper.toTicketDto(ticket)).thenReturn(ticketDto);

        // Act
        List<TicketDto> result = ticketService.getUserTickets(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticketDto, result.get(0));
        verify(ticketRepository).findByUser(user);
        verify(entityMapper).toTicketDto(ticket);
    }

    @Test
    @DisplayName("Успешное получение билетов мероприятия")
    void shouldGetEventTicketsSuccessfully() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(ticketRepository.findByEvent(event)).thenReturn(List.of(ticket));
        when(entityMapper.toTicketDto(ticket)).thenReturn(ticketDto);

        // Act
        List<TicketDto> result = ticketService.getEventTickets(event.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticketDto, result.get(0));
        verify(eventRepository).findById(event.getId());
        verify(ticketRepository).findByEvent(event);
        verify(entityMapper).toTicketDto(ticket);
    }

    @Test
    @DisplayName("Ошибка при попытке получить билеты для несуществующего мероприятия")
    void shouldThrowExceptionIfEventNotFound() {
        // Arrange
        when(eventRepository.findById(event.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> ticketService.getEventTickets(event.getId()),
            "Ожидалось исключение при отсутствии мероприятия"
        );

        assertEquals("Мероприятие не найдено с ID: 1", exception.getMessage());
        verify(eventRepository).findById(event.getId());
        verifyNoInteractions(ticketRepository);
    }

    @Test
    @DisplayName("Успешное удаление билета")
    void shouldDeleteTicketSuccessfully() {
        // Arrange
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        // Act
        ticketService.deleteTicket(ticket.getId(), user);

        // Assert
        verify(ticketRepository).findById(ticket.getId());
        verify(ticketRepository).delete(ticket);
    }

    @Test
    @DisplayName("Ошибка при попытке удалить билет другого пользователя")
    void shouldThrowExceptionIfUserNotOwnerOfTicket() {
        // Arrange
        User anotherUser = new User(2, "anotherUser", "another@example.com", "password");
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> ticketService.deleteTicket(ticket.getId(), anotherUser),
            "Ожидалось исключение при удалении билета другого пользователя"
        );

        assertEquals("Пользователь не является владельцем билета", exception.getMessage());
        verify(ticketRepository).findById(ticket.getId());
        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего билета")
    void shouldThrowExceptionIfTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> ticketService.deleteTicket(ticket.getId(), user),
            "Ожидалось исключение при отсутствии билета"
        );

        assertEquals("Билет не найден с ID: 1", exception.getMessage());
        verify(ticketRepository).findById(ticket.getId());
    }
}

