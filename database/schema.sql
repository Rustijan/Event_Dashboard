DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS event_sessions;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(300) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('PARTICIPANT', 'ORGANIZER')),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE locations (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(120) NOT NULL,
    city     VARCHAR(100) NOT NULL,
    address  VARCHAR(200) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    UNIQUE (city, address)
);

CREATE TABLE events (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(160) NOT NULL,
    description  TEXT NOT NULL,
    category_id  BIGINT NOT NULL REFERENCES categories(id),
    location_id  BIGINT NOT NULL REFERENCES locations(id),
    organizer_id BIGINT NOT NULL REFERENCES users(id),
    status       VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_sessions (
    id         BIGSERIAL PRIMARY KEY,
    event_id   BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    starts_at  TIMESTAMP NOT NULL,
    ends_at    TIMESTAMP NOT NULL,
    capacity   INTEGER NOT NULL CHECK (capacity > 0),
    CONSTRAINT valid_session_period CHECK (ends_at > starts_at)
);

CREATE TABLE registrations (
    id             BIGSERIAL PRIMARY KEY,
    session_id     BIGINT NOT NULL REFERENCES event_sessions(id),
    participant_id BIGINT NOT NULL REFERENCES users(id),
    status         VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'CANCELLED')),
    registered_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at   TIMESTAMP,
    UNIQUE (session_id, participant_id)
);

CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_category ON events(category_id);
CREATE INDEX idx_sessions_event ON event_sessions(event_id);
CREATE INDEX idx_sessions_start ON event_sessions(starts_at);
CREATE INDEX idx_registrations_participant ON registrations(participant_id);
CREATE INDEX idx_registrations_session_status ON registrations(session_id, status);

