-- =======================================
-- Тест 1: Создание мероприятия
-- =======================================
-- Подготовка данных для тестирования
INSERT INTO Location (country, region, city, address)
VALUES ('Country Test', 'Region Test', 'City Test', 'Street 1');

INSERT INTO Venue (name, location_id, capacity)
VALUES ('Test Venue', (SELECT id FROM Location WHERE city = 'City Test'), 100);

INSERT INTO Category (name) VALUES ('Test Category');

-- Вызов функции для создания мероприятия
SELECT create_event(
               'Test Event'::VARCHAR,
               'This is a test event'::TEXT,
               (NOW() + INTERVAL '1 day')::TIMESTAMP,
               (SELECT id FROM Venue WHERE id = 1)::INT,
               (SELECT id FROM Category WHERE id = 1)::INT,
               (SELECT id FROM App_User WHERE username = 'user4')::INT
           ) AS event_id;


-- Ожидаемый результат: ровно 1 запись в таблице.
SELECT * FROM Event WHERE id = 3;


-- =======================================
-- Тест 2: Регистрация участника на мероприятие
-- =======================================
-- Предварительная вставка пользователей и мероприятия
INSERT INTO App_User (username, email, password)
VALUES ('user12', 'user12@test.com', 'pass2');

-- Вызов функции регистрации участника на мероприятие
SELECT register_participant(
               (SELECT id FROM App_User WHERE username = 'user12'),
               3
           );

-- Ожидаемый результат: Успешная регистрация, пользователь должен быть добавлен в таблицу Participant
SELECT * FROM Participant WHERE user_id = (SELECT id FROM App_User WHERE username = 'user12')
AND event_id = (SELECT id FROM Event WHERE name = 'Test Event');

-- =======================================
-- Тест 3: Оставление отзыва о мероприятии
-- =======================================
SELECT leave_review(5, 3, '5', 'Great event!');

-- Ожидаемый результат: отзыв добавлен в таблицу Review
SELECT * FROM Review WHERE user_id = (SELECT id FROM App_User WHERE username = 'user12')
AND event_id = 3;

-- =======================================
-- Тест 4: Добавление информации о еде на мероприятие
-- =======================================
SELECT add_food_to_event(
    4,  -- Организатор
    3,
    'Sandwich',
    'A vegetarian sandwich'
);

-- Ожидаемый результат: Еда должна быть добавлена и связана с мероприятием
SELECT * FROM Event_Food WHERE event_id = (SELECT id FROM Event WHERE name = 'Test Event');
SELECT * FROM Food WHERE name = 'Sandwich';


-- =======================================
-- Тест 5: Изменение категории мероприятия
-- =======================================
-- Добавляем новую категорию
INSERT INTO Category (name) VALUES ('Updated Category');

CALL update_event_category(3, 3);


-- Ожидаемый результат: Категория мероприятия изменена
SELECT * FROM Event WHERE id = 3;

-- =======================================
-- Тест 6: Изменение вместимости мероприятия (Процедура update_event_capacity)
-- =======================================
CALL update_event_capacity(3, 150);

-- Ожидаемый результат: Вместимость изменена
SELECT capacity FROM Venue WHERE id = 1;
