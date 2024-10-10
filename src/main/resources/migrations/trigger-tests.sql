-- Подготовка необходимых записей перед созданием записи в Event
INSERT INTO Location (country, region, city, address)
VALUES ('Test-Country', 'Test-Region', 'Test-City', 'Test-Address');

INSERT INTO Venue (name, location_id, capacity)
VALUES ('Test-Venue',
        (SELECT id FROM Location WHERE country = 'Test-Country' AND city = 'Test-City'),
        3);

INSERT INTO Category (name) VALUES ('Test-Category');

-- Данные подготовлены, можно создавать запись в Event
INSERT INTO Event (name, description, date, venue_id, category_id)
VALUES ('Test-Event',
        'Testing event creation',
        NOW() + INTERVAL '1 day',
        (SELECT id FROM Venue WHERE name = 'Test-Venue'),
        (SELECT id FROM Category WHERE name = 'Test-Category'));


-- Обновляем событие, чтобы проверить, что `updated_at` обновляется
UPDATE Event
SET name = 'Updated Test Event'
WHERE id = 1;


-- Проверка триггера для проверки даты события
-- Попытка вставить событие с датой в прошлом (должно сгенерировать ошибку)
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

-- Проверка триггера на создание уведомлений

-- Добавляем тестовых участников для проверки уведомлений
INSERT INTO App_User (username, email, password)
VALUES ('user1', 'user1@test.com', 'pass1');

INSERT INTO App_User (username, email, password)
VALUES ('user2', 'user2@test.com', 'pass2');

-- Добавляем тестовых участников для существующего события
INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user1'),
        (SELECT id FROM Event WHERE name = 'Test-Event'), true);

INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user2'),
        (SELECT id FROM Event WHERE name = 'Test-Event'), false);


-- Создаем новое событие, чтобы проверить, что уведомления создаются
INSERT INTO Event (name, description, date, venue_id, category_id)
VALUES ('Notification Test Event',
        'This event tests notification creation',
        NOW() + INTERVAL '2 days',
        (SELECT id FROM Venue WHERE name = 'Test-Venue'),
        (SELECT id FROM Category WHERE name = 'Test-Category'));

SELECT * FROM Notification WHERE event_id = (SELECT id FROM Event WHERE name = 'Notification Test Event');

-- Добавление новых участников до достижения вместимости (capacity = 3)
INSERT INTO App_User (username, email, password)
VALUES ('user3', 'user3@test.com', 'pass3');

INSERT INTO Participant (user_id, event_id, is_creator)
VALUES ((SELECT id FROM App_User WHERE username = 'user3'),
        (SELECT id FROM Event WHERE name = 'Test-Event'), false);

-- Проверка триггера на вместимость события
-- Ожидаемая ошибка при попытке добавить больше участников, чем разрешено вместимостью
INSERT INTO App_User (username, email, password)
VALUES ('user4', 'user4@test.com', 'pass4');

DO $$
    BEGIN
        INSERT INTO Participant (user_id, event_id, is_creator)
        VALUES ((SELECT id FROM App_User WHERE username = 'user4'),
                (SELECT id FROM Event WHERE name = 'Test-Event'), false);
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Trigger successfully prevented participant addition beyond capacity.';
END $$;
