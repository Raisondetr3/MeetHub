-- Подготовка необходимых записей перед созданием записи в Event
INSERT INTO Location (country, region, city, address)
VALUES ('Test-Country', 'Test-Region', 'Test-City', 'Test-Address');

INSERT INTO Venue (name, location_id, capacity)
VALUES ('Test-Venue',
        (SELECT id FROM Location WHERE country = 'Test-Country' AND city = 'Test-City'),
        3);

INSERT INTO Category (name) VALUES ('Test-Category');

INSERT INTO Event (name, description, date, venue_id, category_id, updated_at)
VALUES ('Test-Event',
        'Testing event creation',
        NOW() + INTERVAL '1 day',
        (SELECT id FROM Venue WHERE name = 'Test-Venue'),
        (SELECT id FROM Category WHERE name = 'Test-Category'),
        NOW());


-- =======================================
-- Тест 1: Обновление времени изменения события (Триггер set_timestamp)
-- =======================================
UPDATE Event
SET name = 'Updated Test-Event'
WHERE name = 'Test-Event';

-- Проверка результата (должно быть изменено поле updated_at)
SELECT name, updated_at FROM Event WHERE name = 'Updated Test-Event';


-- =======================================
-- Тест 2: Проверка триггера на проверку даты события (Триггер check_event_date)
-- =======================================
-- Попытка вставить событие с датой в прошлом (должно сгенерировать ошибку и не вставить запись)
DO $$
BEGIN
INSERT INTO Event (name, description, date, venue_id, category_id)
VALUES ('Past-Event',
        'This event is in the past',
        NOW() - INTERVAL '1 day',
        (SELECT id FROM Venue WHERE name = 'Test-Venue'),
        (SELECT id FROM Category WHERE name = 'Test-Category'));
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Trigger successfully prevented event creation with a past date.';
END $$;

-- Проверка результата (событие с именем 'Past-Event' не должно существовать)
SELECT * FROM Event WHERE name = 'Past-Event';


-- =======================================
-- Тест 3: Проверка вместимости события (Триггер check_capacity_trigger)
-- =======================================
INSERT INTO App_User (username, email, password)
VALUES ('user1', 'user1@test.com', 'pass1');

INSERT INTO App_User (username, email, password)
VALUES ('user2', 'user2@test.com', 'pass2');

-- Добавление новых участников до достижения вместимости (capacity = 3)
INSERT INTO App_User (username, email, password)
VALUES ('user3', 'user3@test.com', 'pass3');

-- Добавляем тестовых участников для существующего события
INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user1'),
        (SELECT id FROM Event WHERE name = 'Updated Test-Event'), true);

INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user2'),
        (SELECT id FROM Event WHERE name = 'Updated Test-Event'), false);

INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user3'),
        (SELECT id FROM Event WHERE name = 'Updated Test-Event'), false);

-- Проверка триггера на вместимость события
-- Ожидаемая ошибка при попытке добавить больше участников, чем разрешено вместимостью
INSERT INTO App_User (username, email, password)
VALUES ('user4', 'user4@test.com', 'pass4');

DO $$
BEGIN
INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user4'),
        (SELECT id FROM Event WHERE name = 'Updated Test-Event'), false);
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Trigger successfully prevented participant addition beyond capacity.';
END $$;

-- Проверка, что 'user4' не был добавлен
SELECT * FROM Participant WHERE event_id = (SELECT id FROM Event WHERE name = 'Updated Test-Event')
                            AND user_id = (SELECT id FROM App_User WHERE username = 'user4');



