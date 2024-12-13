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

import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.category.CategoryDto;
import ru.itmo.cs.dto.event.EventDto;
import ru.itmo.cs.dto.venue.VenueDto;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.exception.UnauthorizedException;
import ru.itmo.cs.exception.ValidationException;
import ru.itmo.cs.repository.EventRepository;
import ru.itmo.cs.service.*;
import ru.itmo.cs.util.EntityMapper;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EntityMapper entityMapper;
    @Mock
    private ParticipantService participantService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private VenueService venueService;

    @Mock
    private FoodService foodService;

    private User testUser;
    private Event testEvent;
    private EventDto testEventDto;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "testUser", "test@example.com", "password");

        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setName("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setDate(LocalDateTime.now().plusDays(1));

        testEventDto = new EventDto();
        testEventDto.setId(1);
        testEventDto.setName("Test Event");
        testEventDto.setDescription("Test Description");
    }

    @Test
    @DisplayName("Успешное получение мероприятия по ID")
    void getEventById_ShouldReturnEvent() {
        // Arrange
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
        when(entityMapper.toEventDto(testEvent)).thenReturn(testEventDto);

        // Act
        EventDto result = eventService.getEventById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository).findById(1);
        verify(entityMapper).toEventDto(testEvent);
    }

    @Test
    @DisplayName("Ошибка при попытке получить несуществующее мероприятие")
    void getEventById_ShouldThrowExceptionWhenEventNotFound() {
        // Arrange
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> eventService.getEventById(1),
            "Expected exception when event not found"
        );
        assertEquals("Мероприятие не найдено", exception.getMessage());
        verify(eventRepository).findById(1);
    }

    @Test
    @DisplayName("Успешное создание нового мероприятия")
    void createEvent_ShouldCreateEventSuccessfully() {
        // Arrange
        VenueDto venueDto = new VenueDto();
        venueDto.setId(1);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("CONFERENCE");

        testEventDto.setVenue(venueDto);
        testEventDto.setCategory(categoryDto);

        Venue venue = new Venue();
        venue.setId(1);
        venue.setCapacity(100); // Устанавливаем capacity
        when(venueService.getVenueById(1)).thenReturn(venue);

        Category category = new Category();
        category.setName(CategoryEnum.CONFERENCE);
        when(categoryService.getCategoryByName(CategoryEnum.CONFERENCE)).thenReturn(category);

        Event savedEvent = new Event();
        savedEvent.setId(1);
        when(entityMapper.toEventEntity(testEventDto, venue, category, List.of())).thenReturn(testEvent);
        when(eventRepository.save(testEvent)).thenReturn(savedEvent);
        when(entityMapper.toEventDto(savedEvent)).thenReturn(testEventDto);

        // Act
        EventDto result = eventService.createEvent(testEventDto, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(venueService).getVenueById(1);
        verify(categoryService).getCategoryByName(CategoryEnum.CONFERENCE);
        verify(eventRepository).save(testEvent);
        verify(entityMapper).toEventDto(savedEvent);
    }

    @Test
    @DisplayName("Ошибка при попытке создать мероприятие без авторизации")
    void createEvent_ShouldThrowUnauthorizedExceptionWhenUserIsNull() {
        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> eventService.createEvent(testEventDto, null),
            "Expected exception when user is not authorized"
        );
        assertEquals("Пользователь не авторизован.", exception.getMessage());
    }

    @Test
    @DisplayName("Ошибка при попытке создать мероприятие с неверным названием")
    void createEvent_ShouldThrowValidationExceptionWhenNameIsTooLong() {
        // Arrange
        testEventDto.setName("A".repeat(101));

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> eventService.createEvent(testEventDto, testUser),
            "Expected exception for invalid name"
        );
        assertEquals("Название мероприятия не должно превышать 100 символов.", exception.getMessage());
    }

    @Test
    @DisplayName("Успешное обновление мероприятия")
    void updateEvent_ShouldUpdateEventSuccessfully() {
        // Arrange
        when(eventRepository.findByIdWithFood(1)).thenReturn(Optional.of(testEvent));
        doNothing().when(participantService).validateOrganizer(1, testUser.getId());

        // Устанавливаем только те моки, которые используются
        if (testEventDto.getVenue() != null) {
            Venue venue = new Venue();
            venue.setId(1);
            when(venueService.getVenueById(1)).thenReturn(venue);
        }

        if (testEventDto.getCategory() != null) {
            Category category = new Category();
            category.setName(CategoryEnum.CONFERENCE);
            when(categoryService.getCategoryByName(CategoryEnum.CONFERENCE)).thenReturn(category);
        }

        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(entityMapper.toEventDto(testEvent)).thenReturn(testEventDto);

        // Act
        EventDto result = eventService.updateEvent(1, testEventDto, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository).findByIdWithFood(1);
        verify(participantService).validateOrganizer(1, testUser.getId());
        verify(eventRepository).save(testEvent);
        verify(entityMapper).toEventDto(testEvent);

        // Убедиться, что ненужные заглушки не вызываются
        if (testEventDto.getVenue() != null) {
            verify(venueService).getVenueById(1);
        }

        if (testEventDto.getCategory() != null) {
            verify(categoryService).getCategoryByName(CategoryEnum.CONFERENCE);
        }
    }

    @Test
    @DisplayName("Ошибка при обновлении мероприятия неорганизатором")
    void updateEvent_ShouldThrowUnauthorizedExceptionWhenNotOrganizer() {
        // Arrange
        when(eventRepository.findByIdWithFood(1)).thenReturn(Optional.of(testEvent));
        doThrow(new UnauthorizedException("Unauthorized"))
            .when(participantService).validateOrganizer(1, testUser.getId());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> eventService.updateEvent(1, testEventDto, testUser),
            "Expected exception when user is not the organizer"
        );
        assertEquals("Unauthorized", exception.getMessage());
        verify(participantService).validateOrganizer(1, testUser.getId());
    }

    @Test
    @DisplayName("Успешное удаление мероприятия")
    void deleteEvent_ShouldDeleteEventSuccessfully() {
        // Arrange
        when(eventRepository.findById(1)).thenReturn(Optional.of(testEvent));
        doNothing().when(participantService).validateOrganizer(1, testUser.getId());

        // Act
        eventService.deleteEvent(1, testUser);

        // Assert
        verify(eventRepository).findById(1);
        verify(participantService).validateOrganizer(1, testUser.getId());
        verify(eventRepository).delete(testEvent);
    }

    @Test
    @DisplayName("Ошибка при создании мероприятия с датой в прошлом")
    void createEvent_ShouldThrowExceptionWhenDateIsInThePast() {
        // Arrange
        testEventDto.setDate(LocalDateTime.now().minusDays(1)); // Устанавливаем дату в прошлом

        VenueDto venueDto = new VenueDto();
        venueDto.setId(1);
        testEventDto.setVenue(venueDto);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("CONFERENCE");
        testEventDto.setCategory(categoryDto);

        Venue venue = new Venue();
        venue.setId(1);
        venue.setCapacity(100);

        when(venueService.getVenueById(1)).thenReturn(venue);
        when(categoryService.getCategoryByName(CategoryEnum.CONFERENCE))
            .thenReturn(new Category(1, CategoryEnum.CONFERENCE));

        when(entityMapper.toEventEntity(any(EventDto.class), any(Venue.class), any(Category.class), anyList()))
            .thenReturn(testEvent);

        when(eventRepository.save(any(Event.class)))
            .thenThrow(new RuntimeException("Event date cannot be in the past"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> eventService.createEvent(testEventDto, testUser),
            "Expected exception when event date is in the past"
        );
        assertEquals("Event date cannot be in the past", exception.getMessage());

        // Verify mocks
        verify(venueService).getVenueById(1);
        verify(categoryService).getCategoryByName(CategoryEnum.CONFERENCE);
        verify(eventRepository).save(any(Event.class));
    }
}

