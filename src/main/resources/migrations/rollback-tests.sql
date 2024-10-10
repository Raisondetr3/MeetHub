-- Откат тестовых данных, добавленных для проверки триггеров

-- Удаление тестовых событий
DELETE FROM Event WHERE name IN ('Test-Event', 'Updated Test Event', 'Past-Event', 'Notification Test Event');

DELETE FROM Users WHERE email IN ('user1@test.com', 'user2@test.com');

-- Удаление тестовых участников
DELETE FROM Participant WHERE user_id IN (1, 2);

-- Удаление всех уведомлений, созданных во время тестов
DELETE FROM Notification WHERE content LIKE 'New event created:%';

-- Удаление созданных для теста билетов
DELETE FROM Ticket WHERE event_id IN (SELECT id FROM Event WHERE name = 'Test-Event');

-- Дополнительная чистка, если нужно удалить другие тестовые данные
