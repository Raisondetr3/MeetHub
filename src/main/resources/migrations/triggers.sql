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
    IF NEW.date IS NULL THEN
        RAISE EXCEPTION 'Event date cannot be null';
END IF;

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

