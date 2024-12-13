-- 1. Прецедент: Оставление отзыва о мероприятии
CREATE OR REPLACE FUNCTION leave_review(
    _user_id INT,
    _event_id INT,
    _rating rating_enum,
    _comment TEXT
) RETURNS VOID AS $$
BEGIN

    -- Проверка регистрации участника на мероприятие
    IF NOT EXISTS (SELECT 1 FROM Participant WHERE user_id = _user_id AND event_id = _event_id) THEN
        RAISE EXCEPTION 'The user is not registered for this event.';
    END IF;


INSERT INTO Review (user_id, event_id, rating, comment, created_at)
    VALUES (_user_id, _event_id, _rating, _comment, NOW());
END;
$$ LANGUAGE plpgsql;


-- 2. Прецедент: Изменение категории мероприятия
CREATE OR REPLACE PROCEDURE update_event_category(
    event_id INT,
    new_category_id INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Event WHERE id = event_id) THEN
        RAISE EXCEPTION 'An event with this ID does not exist.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM Category WHERE id = new_category_id) THEN
        RAISE EXCEPTION 'A category with this ID does not exist.';
    END IF;

    UPDATE Event
    SET category_id = new_category_id
    WHERE id = event_id;

RAISE NOTICE 'The event category has been successfully updated for event ID %', event_id;
END;
$$;


-- 3. Прецедент: Изменение вместимости мероприятия
CREATE OR REPLACE PROCEDURE update_event_capacity(
    _event_id INT,
    _new_capacity INT
)
LANGUAGE plpgsql
AS $$
DECLARE
_current_participants INT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Event WHERE id = _event_id) THEN
        RAISE EXCEPTION 'An event with this ID does not exist.';
    END IF;

    -- Проверка текущего количества участников
    SELECT COUNT(*) INTO _current_participants FROM Participant WHERE event_id = _event_id;

    IF _new_capacity < _current_participants THEN
        RAISE EXCEPTION 'The new capacity cannot be less than the current number of participants.';
    END IF;

    -- Обновление вместимости
    UPDATE Venue
    SET capacity = _new_capacity
    WHERE id = (SELECT venue_id FROM Event WHERE id = _event_id);

RAISE NOTICE 'The event capacity has been successfully updated for the event ID %', _event_id;
END;
$$;
