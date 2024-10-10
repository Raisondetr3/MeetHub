-- Обновление времени последнего изменения в Event
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON Event
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- Проверка даты события в Event
CREATE OR REPLACE FUNCTION check_event_date()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.date < NOW() THEN
        RAISE EXCEPTION 'Event date cannot be in the past';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_event_date_trigger
    BEFORE INSERT OR UPDATE ON Event
                         FOR EACH ROW
                         EXECUTE FUNCTION check_event_date();

-- Создание уведомлений при добавлении нового события
CREATE OR REPLACE FUNCTION create_notification_for_new_event()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO Notification (user_id, event_id, content, sent_at)
SELECT user_id, NEW.id, 'New event created: ' || NEW.name, NOW()
FROM Participant
WHERE event_id = NEW.id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER create_notification_trigger
    AFTER INSERT ON Event
    FOR EACH ROW
    EXECUTE FUNCTION create_notification_for_new_event();

-- Проверка вместимости события
CREATE OR REPLACE FUNCTION check_event_capacity()
RETURNS TRIGGER AS $$
DECLARE
participant_count INT;
    event_capacity INT;
BEGIN
SELECT COUNT(*) INTO participant_count FROM Participant WHERE event_id = NEW.event_id;
SELECT capacity INTO event_capacity FROM Venue WHERE id = (SELECT venue_id FROM Event WHERE id = NEW.event_id);

IF participant_count >= event_capacity THEN
        RAISE EXCEPTION 'Event capacity exceeded. No more participants can be added.';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_capacity_trigger
    BEFORE INSERT ON Participant
    FOR EACH ROW
    EXECUTE FUNCTION check_event_capacity();
