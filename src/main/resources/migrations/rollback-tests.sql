DELETE FROM Participant
WHERE event_id IN (1, 3, 4);

DELETE FROM Review
WHERE user_id IN (SELECT id FROM App_User WHERE username IN ('user1', 'user2', 'user3', 'user4', 'user12'));

DELETE FROM App_User
WHERE username IN ('user1', 'user2', 'user3', 'user4', 'user12');

DELETE FROM Event_Food
WHERE event_id IN (1, 3, 4);

DELETE FROM Event
WHERE id IN (1, 3, 4);

DELETE FROM Category
WHERE id IN (1, 2);

DELETE FROM Venue
WHERE id IN (1, 2, 4);

DELETE FROM Location
WHERE id IN (1, 2);
