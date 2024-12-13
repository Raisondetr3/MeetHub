package ru.itmo.cs.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.entity.Event;
import ru.itmo.cs.entity.Participant;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.exception.UnauthorizedException;
import ru.itmo.cs.repository.ParticipantRepository;

/**
 * Сервис для управления участниками мероприятий.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    /**
     * Регистрирует участника мероприятия.
     *
     * @param participant участник
     * @return зарегистрированный участник
     */
    public Participant registerParticipant(Participant participant) {
        return participantRepository.save(participant);
    }

    /**
     * Возвращает список участников мероприятия.
     *
     * @param eventId ID мероприятия
     * @return список участников
     */
    public List<Participant> getParticipantsByEvent(Integer eventId) {
        return participantRepository.findByEventId(eventId);
    }

    /**
     * Возвращает участника по составному ключу.
     *
     * @param participantId составной ключ участника
     * @return объект Participant
     * @throws ResourceNotFoundException если участник не найден
     */
    @Transactional(readOnly = true)
    public Participant getParticipantById(Participant.ParticipantId participantId) {
        return participantRepository.findById(participantId)
            .orElseThrow(() -> new ResourceNotFoundException("Участник не найден с указанным ID."));
    }

    /**
     * Проверяет существование участника по ID пользователя и ID мероприятия.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     * @return true, если участник уже зарегистрирован
     */
    public boolean isParticipantExists(Integer userId, Integer eventId) {
        return participantRepository.existsByUserIdAndEventId(userId, eventId);
    }

    /**
     * Удаляет участника из мероприятия.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     * @throws ResourceNotFoundException если участник не найден
     */
    @Transactional
    public void deleteParticipant(Integer userId, Integer eventId) {
        if (!participantRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ResourceNotFoundException("Участник не найден с указанным ID.");
        }
        Participant.ParticipantId participantId = new Participant.ParticipantId(userId, eventId);
        participantRepository.deleteById(participantId);
    }

    /**
     * Проверяет, является ли пользователь организатором мероприятия.
     *
     * @param eventId ID мероприятия
     * @param userId  ID пользователя
     * @throws UnauthorizedException если пользователь не является организатором
     */
    public void validateOrganizer(Integer eventId, Integer userId) {
        if (!participantRepository.isOrganizer(eventId, userId)) {
            throw new UnauthorizedException("Вы не являетесь организатором данного мероприятия");
        }
    }

    /**
     * Регистрирует пользователя как организатора мероприятия.
     *
     * @param user  ID пользователя
     * @param event ID мероприятия
     * @return зарегистрированный объект Participant
     */
    @Transactional
    public Participant registerOrganizer(User user, Event event) {
        if (participantRepository.existsByUserIdAndEventId(user.getId(), event.getId())) {
            throw new IllegalStateException("Пользователь уже зарегистрирован для данного мероприятия");
        }

        Participant.ParticipantId participantId = new Participant.ParticipantId(user.getId(), event.getId());
        Participant organizer = new Participant(participantId, user, event, true);

        return participantRepository.save(organizer);
    }

    /**
     * Находит организатора мероприятия.
     *
     * @param event мероприятие
     * @return объект Participant (организатор)
     */
    public Optional<Participant> findEventCreator(Event event) {
        return participantRepository.findEventCreator(event);
    }
}