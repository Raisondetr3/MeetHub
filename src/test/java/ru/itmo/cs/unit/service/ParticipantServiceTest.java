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
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.exception.UnauthorizedException;
import ru.itmo.cs.repository.ParticipantRepository;
import ru.itmo.cs.service.ParticipantService;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;

    @Mock
    private ParticipantRepository participantRepository;

    private User user;
    private Event event;
    private Participant participant;

    @BeforeEach
    void setUp() {
        user = new User(1, "testUser", "test@example.com", "password");
        event = new Event(1, "Test Event", "Test Description", LocalDateTime.now().plusDays(1), null, null, List.of(), LocalDateTime.now());
        participant = new Participant(
            new Participant.ParticipantId(user.getId(), event.getId()),
            user,
            event,
            false
        );
    }

    @Test
    @DisplayName("Успешная регистрация участника")
    void shouldRegisterParticipantSuccessfully() {
        // Arrange
        when(participantRepository.save(participant)).thenReturn(participant);

        // Act
        Participant result = participantService.registerParticipant(participant);

        // Assert
        assertNotNull(result);
        assertEquals(participant, result);
        verify(participantRepository).save(participant);
    }

    @Test
    @DisplayName("Получение списка участников по ID мероприятия")
    void shouldGetParticipantsByEvent() {
        // Arrange
        when(participantRepository.findByEventId(event.getId())).thenReturn(List.of(participant));

        // Act
        List<Participant> result = participantService.getParticipantsByEvent(event.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(participant, result.get(0));
        verify(participantRepository).findByEventId(event.getId());
    }

    @Test
    @DisplayName("Получение участника по составному ключу")
    void shouldGetParticipantById() {
        // Arrange
        Participant.ParticipantId participantId = participant.getId();
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Act
        Participant result = participantService.getParticipantById(participantId);

        // Assert
        assertNotNull(result);
        assertEquals(participant, result);
        verify(participantRepository).findById(participantId);
    }

    @Test
    @DisplayName("Ошибка при отсутствии участника по составному ключу")
    void shouldThrowExceptionIfParticipantNotFoundById() {
        // Arrange
        Participant.ParticipantId participantId = participant.getId();
        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> participantService.getParticipantById(participantId),
            "Ожидалось исключение при отсутствии участника"
        );

        assertEquals("Участник не найден с указанным ID.", exception.getMessage());
    }

    @Test
    @DisplayName("Проверка существования участника")
    void shouldCheckIfParticipantExists() {
        // Arrange
        when(participantRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(true);

        // Act
        boolean result = participantService.isParticipantExists(user.getId(), event.getId());

        // Assert
        assertTrue(result);
        verify(participantRepository).existsByUserIdAndEventId(user.getId(), event.getId());
    }

    @Test
    @DisplayName("Удаление участника по ID")
    void shouldDeleteParticipantSuccessfully() {
        // Arrange
        when(participantRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(true);

        // Act
        participantService.deleteParticipant(user.getId(), event.getId());

        // Assert
        verify(participantRepository).existsByUserIdAndEventId(user.getId(), event.getId());
        verify(participantRepository).deleteById(new Participant.ParticipantId(user.getId(), event.getId()));
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего участника")
    void shouldThrowExceptionWhenDeletingNonExistentParticipant() {
        // Arrange
        when(participantRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> participantService.deleteParticipant(user.getId(), event.getId()),
            "Ожидалось исключение при попытке удалить несуществующего участника"
        );

        assertEquals("Участник не найден с указанным ID.", exception.getMessage());
    }

    @Test
    @DisplayName("Проверка пользователя как организатора")
    void shouldValidateOrganizerSuccessfully() {
        // Arrange
        when(participantRepository.isOrganizer(event.getId(), user.getId())).thenReturn(true);

        // Act
        participantService.validateOrganizer(event.getId(), user.getId());

        // Assert
        verify(participantRepository).isOrganizer(event.getId(), user.getId());
    }

    @Test
    @DisplayName("Ошибка при проверке неорганизатора")
    void shouldThrowExceptionIfUserIsNotOrganizer() {
        // Arrange
        when(participantRepository.isOrganizer(event.getId(), user.getId())).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> participantService.validateOrganizer(event.getId(), user.getId()),
            "Ожидалось исключение при проверке пользователя как неорганизатора"
        );

        assertEquals("Вы не являетесь организатором данного мероприятия", exception.getMessage());
    }

    @Test
    @DisplayName("Успешная регистрация организатора")
    void shouldRegisterOrganizerSuccessfully() {
        // Arrange
        when(participantRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(false);
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> {
            Participant savedParticipant = invocation.getArgument(0);
            savedParticipant.setId(new Participant.ParticipantId(user.getId(), event.getId()));
            return savedParticipant;
        });

        // Act
        Participant result = participantService.registerOrganizer(user, event);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(event, result.getEvent());
        assertTrue(result.getIsCreator());
        verify(participantRepository).existsByUserIdAndEventId(user.getId(), event.getId());
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    @DisplayName("Ошибка при регистрации существующего организатора")
    void shouldThrowExceptionIfOrganizerAlreadyExists() {
        // Arrange
        when(participantRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> participantService.registerOrganizer(user, event),
            "Ожидалось исключение при попытке зарегистрировать существующего организатора"
        );

        assertEquals("Пользователь уже зарегистрирован для данного мероприятия", exception.getMessage());
    }
}

