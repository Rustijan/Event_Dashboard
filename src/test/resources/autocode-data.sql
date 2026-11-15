-- Same logical dataset as database/data.sql. Dates are relative to every test run.
-- Both demo users use password: Password123!

INSERT INTO users (full_name, email, password_hash, role) VALUES
('Olivia Organizer', 'organizer@eventhub.local', '120000:9b9f1080506993556f5b38f714e099ae:798b122164f97412c04e0228589d92b10ae7acc6fa3a165e6dd565114ed9580d', 'ORGANIZER'),
('Peter Participant', 'participant@eventhub.local', '120000:39ab55e5979aa37b6c2687fda47b28d0:b79d6d7646000b824c0b156e12921a8771c883db0b08eb189594a54b6f7ecce0', 'PARTICIPANT');

INSERT INTO categories (name) VALUES
('Technology'), ('Education'), ('Community'), ('Arts'), ('Wellness');

INSERT INTO locations (name, city, address, capacity) VALUES
('Innovation Hall', 'Minsk', '1 Independence Avenue', 120),
('Community Room', 'Minsk', '25 Victory Street', 40),
('Creative Studio', 'Minsk', '8 Culture Lane', 25);

INSERT INTO events (title, description, category_id, location_id, organizer_id, status) VALUES
('Java Community Evening', 'An evening of short talks, practical demonstrations, and discussion for Java learners.', 1, 1, 1, 'PUBLISHED'),
('Clean Code Workshop', 'A practical workshop focused on naming, small methods, and readable object-oriented code.', 2, 2, 1, 'PUBLISHED'),
('Spring MVC Practice', 'A draft hands-on session for building a server-side Spring MVC application.', 1, 3, 1, 'DRAFT');

INSERT INTO event_sessions (event_id, starts_at, ends_at, capacity) VALUES
(1, DATEADD('DAY', 7, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 7, CURRENT_TIMESTAMP)), 60),
(1, DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 60),
(2, DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 10, CURRENT_TIMESTAMP)), 30),
(3, DATEADD('DAY', 20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 20, CURRENT_TIMESTAMP)), 20);
