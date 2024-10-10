DROP TRIGGER IF EXISTS set_timestamp ON Event;
DROP FUNCTION IF EXISTS update_timestamp;

DROP TRIGGER IF EXISTS check_event_date_trigger ON Event;
DROP FUNCTION IF EXISTS check_event_date;

DROP TRIGGER IF EXISTS create_notification_trigger ON Event;
DROP FUNCTION IF EXISTS create_notification_for_new_event;

DROP TRIGGER IF EXISTS check_capacity_trigger ON Participant;
DROP FUNCTION IF EXISTS check_event_capacity;
