-- 1. Прецедент: Создание мероприятия
CREATE OR REPLACE FUNCTION create_event(
    _name VARCHAR,
    _description TEXT,
    _date TIMESTAMP,
    _venue_id INT,
    _category_id INT,
    _organizer_id INT
) RETURNS INT AS $$
DECLARE
_event_id INT;
BEGIN
    IF _name IS NULL OR _date IS NULL THEN
        RAISE EXCEPTION 'The name of the event and the date are required.';
    END IF;

INSERT INTO Event (name, description, date, venue_id, category_id, updated_at)
VALUES (_name, _description, _date, _venue_id, _category_id, NOW())
    RETURNING id INTO _event_id;

INSERT INTO Participant (user_id, event_id, is_creator)
VALUES (_organizer_id, _event_id, TRUE);

RETURN _event_id;
END;
$$ LANGUAGE plpgsql;


-- 2. Прецедент: Регистрация на мероприятие
CREATE OR REPLACE FUNCTION register_participant(
    _user_id INT,
    _event_id INT
) RETURNS VOID AS $$
DECLARE
_current_participants INT;
    _max_participants INT;
BEGIN
    -- Получение количества зарегистрированных участников
SELECT COUNT(*) INTO _current_participants FROM Participant WHERE event_id = _event_id;
SELECT capacity INTO _max_participants FROM Venue WHERE id = (SELECT venue_id FROM Event WHERE id = _event_id);

    IF _current_participants >= _max_participants THEN
        RAISE EXCEPTION 'The maximum number of participants has been reached.';
    END IF;

INSERT INTO Participant (user_id, event_id, is_creator)
VALUES (_user_id, _event_id, FALSE);
END;
$$ LANGUAGE plpgsql;


-- 3. Прецедент: Оставление отзыва о мероприятии
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


-- 4. Прецедент: Добавление информации о еде на мероприятии
CREATE OR REPLACE FUNCTION add_food_to_event(
    _organizer_id INT,
    _event_id INT,
    _food_name VARCHAR,
    _description TEXT
) RETURNS VOID AS $$
DECLARE
_food_id INT;
BEGIN
    -- Проверка, что пользователь является организатором
    IF NOT EXISTS (SELECT 1 FROM Participant WHERE user_id = _organizer_id AND event_id = _event_id AND is_creator = TRUE) THEN
        RAISE EXCEPTION 'The user is not the organizer of the event.';
    END IF;

    -- Вставка новой еды
INSERT INTO Food (name, composition)
VALUES (_food_name, _description)
    RETURNING id INTO _food_id;

-- Связывание еды с мероприятием
INSERT INTO Event_Food (event_id, food_id)
VALUES (_event_id, _food_id);
END;
$$ LANGUAGE plpgsql;


-- 5. Прецедент: Изменение категории мероприятия
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


-- 6. Прецедент: Изменение вместимости мероприятия
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
