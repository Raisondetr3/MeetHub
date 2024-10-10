
DELETE FROM Event WHERE id = 3;

DELETE FROM Venue WHERE name = 'Test Venue';
DELETE FROM Location WHERE city = 'City Test';
DELETE FROM Category WHERE name = 'Test Category';


DELETE FROM Participant WHERE user_id = (SELECT id FROM App_User WHERE username = 'user12')
                          AND event_id = 3;

DELETE FROM App_User WHERE username = 'user12';

DELETE FROM Review WHERE user_id = (SELECT id FROM App_User WHERE username = 'user12')
                     AND event_id = 3;

DELETE FROM Event_Food WHERE event_id = 1 AND food_id = (SELECT id FROM Food WHERE name = 'Sandwich');

DELETE FROM Food WHERE name = 'Sandwich';

UPDATE Event SET category_id = (SELECT id FROM Category WHERE name = 'Test Category')
WHERE id = 1;

DELETE FROM Category WHERE name = 'Updated Category';

UPDATE Venue SET capacity = 100 WHERE id = 1;
