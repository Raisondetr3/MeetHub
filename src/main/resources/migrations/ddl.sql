CREATE TABLE IF NOT EXISTS App_User (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Location (
                          id SERIAL PRIMARY KEY,
                          country VARCHAR(50) NOT NULL,
                          region VARCHAR(50) NOT NULL,
                          city VARCHAR(50) NOT NULL,
                          address VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Venue (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       location_id INT NOT NULL UNIQUE REFERENCES Location(id),
                       capacity INT NOT NULL
);

CREATE TABLE IF NOT EXISTS Category (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Event (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       description TEXT,
                       date TIMESTAMP NOT NULL,
                       venue_id INT NOT NULL REFERENCES Venue(id),
                       category_id INT NOT NULL REFERENCES Category(id)
);

CREATE TABLE IF NOT EXISTS Food (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(100) NOT NULL,
                      composition TEXT
);

CREATE TABLE IF NOT EXISTS Review (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL REFERENCES App_User(id),
                        event_id INT NOT NULL REFERENCES Event(id),
                        rating rating_enum NOT NULL,
                        comment TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE TABLE IF NOT EXISTS Ticket (
                        id SERIAL PRIMARY KEY,
                        seat_number VARCHAR(10),
                        event_id INT NOT NULL REFERENCES Event(id),
                        user_id INT NOT NULL REFERENCES App_User(id)
);

CREATE TABLE IF NOT EXISTS Notification (
                              id SERIAL PRIMARY KEY,
                              user_id INT NOT NULL REFERENCES App_User(id),
                              event_id INT NOT NULL REFERENCES Event(id),
                              content TEXT NOT NULL,
                              sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS Participant (
                             user_id INT NOT NULL REFERENCES App_User(id),
                             event_id INT NOT NULL REFERENCES Event(id),
                             is_creator BOOLEAN DEFAULT FALSE NOT NULL,
                             PRIMARY KEY (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS Event_Food (
                            event_id INT NOT NULL REFERENCES Event(id),
                            food_id INT NOT NULL REFERENCES Food(id),
                            PRIMARY KEY (event_id, food_id)
);
