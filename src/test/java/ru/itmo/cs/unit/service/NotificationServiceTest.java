package ru.itmo.cs.unit.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import ru.itmo.cs.dto.notification.NotificationDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Notification;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.NotificationRepository;
import ru.itmo.cs.repository.ParticipantRepository;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.service.NotificationService;
import ru.itmo.cs.util.EntityMapper;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EntityMapper entityMapper;

    private User user;
    private Event event;
    private Notification notification;
    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        user = new User(1, "testUser", "test@example.com", "password");
        event = new Event(1, "Test Event", "Test Description", LocalDateTime.now().plusDays(1), null, null, List.of(), LocalDateTime.now());
        notification = new Notification(1, user, event, "Test Notification", "SENT", new Date());
        notificationDto = new NotificationDto(1, 1, "testUser", "test@example.com", 1, "Test Event", "Test Notification", "SENT", new Date());
    }

    @Test
    @DisplayName("Успешное создание уведомления")
    void shouldCreateNotificationSuccessfully() {
        // Arrange
        when(entityMapper.toNotificationEntity(notificationDto, user, event)).thenReturn(notification);
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(entityMapper.toNotificationDto(notification)).thenReturn(notificationDto);

        // Act
        NotificationDto result = notificationService.createNotification(notificationDto, user, event);

        // Assert
        assertNotNull(result);
        assertEquals(notificationDto, result);
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(notificationRepository).save(notification);
        verify(entityMapper).toNotificationDto(notification);
    }

    @Test
    @DisplayName("Отправка напоминаний участникам")
    void shouldSendRemindersToParticipants() {
        // Arrange
        Participant participant = new Participant(new Participant.ParticipantId(1, 1), user, event, false);
        Notification notificationMock = new Notification(null, user, event, "Reminder Content", "SENT", new Date());
        when(participantRepository.findByEventId(event.getId())).thenReturn(List.of(participant));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification passedNotification = invocation.getArgument(0);
            passedNotification.setId(1);
            return passedNotification;
        });
        when(entityMapper.toNotificationEntity(any(NotificationDto.class), eq(user), eq(event))).thenReturn(notificationMock);

        // Act
        notificationService.sendReminderToParticipants(event);

        // Assert
        verify(participantRepository).findByEventId(event.getId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }


    @Test
    @DisplayName("Ошибка при отправке напоминаний за пределами 24 часов до события")
    void shouldThrowExceptionIfReminderSentOutside24Hours() {
        // Arrange
        event.setDate(LocalDateTime.now().plusDays(2));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> notificationService.sendReminderToParticipants(event),
            "Ожидалось исключение при отправке напоминаний за пределами 24 часов"
        );

        assertEquals("Напоминание может быть отправлено только за 24 часа до события", exception.getMessage());
    }

    @Test
    @DisplayName("Успешное получение уведомлений пользователя")
    void shouldGetNotificationsByUserSuccessfully() {
        // Arrange
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(notificationRepository.findByUserId(user.getId())).thenReturn(List.of(notification));
        when(entityMapper.toNotificationDto(notification)).thenReturn(notificationDto);

        // Act
        List<NotificationDto> result = notificationService.getNotificationsByUser(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(notificationDto, result.get(0));
        verify(userRepository).existsById(user.getId());
        verify(notificationRepository).findByUserId(user.getId());
        verify(entityMapper).toNotificationDto(notification);
    }

    @Test
    @DisplayName("Ошибка при отсутствии уведомлений у пользователя")
    void shouldThrowExceptionIfNotificationsNotFoundForUser() {
        // Arrange
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(notificationRepository.findByUserId(user.getId())).thenReturn(List.of());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> notificationService.getNotificationsByUser(user.getId()),
            "Ожидалось исключение при отсутствии уведомлений"
        );

        assertEquals("Уведомления для пользователя с ID 1 не найдены", exception.getMessage());
    }

    @Test
    @DisplayName("Успешная отправка email")
    void shouldSendEmailSuccessfully() {
        // Arrange
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Test Subject");
        mailMessage.setText("Test Body");

        // Act
        notificationService.sendEmail(user.getEmail(), "Test Subject", "Test Body");

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}