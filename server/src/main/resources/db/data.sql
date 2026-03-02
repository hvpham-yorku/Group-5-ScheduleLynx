-- username=demo, password=demo123
-- NOTE: this hash must match whatever hashing you use in UserService.
INSERT INTO users (username, password_hash)
VALUES ('demo', 'demo123')
ON CONFLICT (username) DO NOTHING;

-- Seed content for demo (assumes demo has id=1 if fresh DB)
INSERT INTO tasks (user_id, title, due_date, estimated_hours, difficulty)
SELECT id, 'Sample Task', CURRENT_DATE + INTERVAL '3 day', 2, 'LOW'
FROM users WHERE username='demo'
ON CONFLICT DO NOTHING;

INSERT INTO availability (user_id, day, start_time, end_time)
SELECT id, 'MONDAY', '18:00:00', '21:00:00'
FROM users WHERE username='demo';

INSERT INTO events (user_id, title, day, start_time, end_time)
SELECT id, 'Sample Event', 'TUESDAY', '19:00:00', '20:00:00'
FROM users WHERE username='demo';
