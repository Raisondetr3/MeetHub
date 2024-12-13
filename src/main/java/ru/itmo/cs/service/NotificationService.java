package ru.itmo.cs.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.notification.NotificationDto;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Notification;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.NotificationRepository;
import ru.itmo.cs.repository.ParticipantRepository;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

/**
 * Сервис для управления уведомлениями.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    @Value("${spring.mail.username}")
    private String defaultFrom;
    private final NotificationRepository notificationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final EntityMapper entityMapper;

    /**
     * Создаёт уведомление для пользователя и отправляет email.
     *
     * @param notificationDto DTO уведомления
     * @param user объект пользователя
     * @param event объект мероприятия
     * @return созданное уведомление в виде DTO
     */
    public NotificationDto createNotification(NotificationDto notificationDto, User user, Event event) {
        Notification notification = entityMapper.toNotificationEntity(notificationDto, user, event);
        notification.setStatus("SENT");
        Notification savedNotification = notificationRepository.save(notification);

        sendEmail(
                notification.getUser().getEmail(),
                "Notification: " + notification.getEvent().getName(),
                notification.getContent()
        );

        return entityMapper.toNotificationDto(savedNotification);
    }

    /**
     * Отправляет напоминания участникам мероприятия за 24 часа до его начала.
     *
     * @param event объект мероприятия
     */
    public void sendReminderToParticipants(Event event) {
        if (Duration.between(LocalDateTime.now(), event.getDate()).toHours() > 24) {
            throw new IllegalStateException("Напоминание может быть отправлено только за 24 часа до события");
        }

        List<Participant> participants = participantRepository.findByEventId(event.getId());

        for (Participant participant : participants) {
            String content = String.format(
                    "Уважаемый %s, это напоминание о событии '%s', запланированном на %s.",
                    participant.getUser().getUsername(),
                    event.getName(),
                    event.getDate()
            );

            NotificationDto notificationDto = new NotificationDto(
                    null,
                    participant.getUser().getId(),
                    participant.getUser().getUsername(),
                    participant.getUser().getEmail(),
                    event.getId(),
                    event.getName(),
                    content,
                    "SENT",
                    new Date()
            );

            createNotification(notificationDto, participant.getUser(), event);
        }
    }

    /**
     * Получает уведомления по идентификатору пользователя
     *
     * @param userId идентификатор пользователя
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь не найден");
        }

        List<Notification> notifications = notificationRepository.findByUserId(userId);
        if (notifications.isEmpty()) {
            throw new ResourceNotFoundException("Уведомления для пользователя с ID " + userId + " не найдены");
        }

        return notifications.stream()
                .map(entityMapper::toNotificationDto)
                .toList();
    }

    /**
     * Отправляет email.
     *
     * @param recipient адрес получателя
     * @param subject   тема письма
     * @param body      тело письма
     */
    public void sendEmail(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(defaultFrom);
        mailSender.send(message);
    }
}