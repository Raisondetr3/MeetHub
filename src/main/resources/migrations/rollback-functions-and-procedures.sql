DROP FUNCTION IF EXISTS create_event(VARCHAR, TEXT, TIMESTAMP, INT, INT, INT);
DROP FUNCTION IF EXISTS register_participant(INT, INT);
DROP FUNCTION IF EXISTS leave_review(INT, INT, rating_enum, TEXT);
DROP FUNCTION IF EXISTS add_food_to_event(INT, INT, VARCHAR, TEXT);

DROP PROCEDURE IF EXISTS update_event_category(INT, INT);
DROP PROCEDURE IF EXISTS update_event_capacity(INT, INT);
