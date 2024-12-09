package ru.itmo.cs.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Notification;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.repository.NotificationRepository;
import ru.itmo.cs.repository.ParticipantRepository;

/**
 * Сервис для управления уведомлениями.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ParticipantRepository participantRepository;
    private final JavaMailSender mailSender;

    /**
     * Создаёт уведомление для пользователя и отправляет email.
     *
     * @param notification объект уведомления
     * @return созданное уведомление
     */
    public Notification createNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);
        sendEmail(
            notification.getUser().getEmail(),
            "Notification: " + notification.getEvent().getName(),
            notification.getContent()
        );
        return savedNotification;
    }

    /**
     * Отправляет напоминания участникам мероприятия за 24 часа до его начала.
     *
     * @param event мероприятие
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

            Notification notification = new Notification();
            notification.setEvent(event);
            notification.setUser(participant.getUser());
            notification.setContent(content);

            notificationRepository.save(notification);
            sendEmail(participant.getUser().getEmail(), "Event Reminder", content);
        }
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
        mailSender.send(message);
    }
}





