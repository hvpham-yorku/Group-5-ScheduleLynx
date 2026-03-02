INSERT INTO users (id, username, password_hash)
VALUES
  (1, 'alice', 'seed'),
  (2, 'bob', 'seed')
ON CONFLICT (id) DO NOTHING;

-- Default tasks for alice (id=1)
INSERT INTO tasks (id, user_id, title, due_date, estimated_hours, difficulty)
VALUES
  (1, 1, 'Seed Task A', '2026-03-06', 2, 'HIGH'),
  (2, 1, 'Seed Task B', '2026-03-03', 1, 'LOW')
ON CONFLICT (id) DO NOTHING;

-- Default availability for alice
INSERT INTO availability_blocks (id, user_id, day, start_time, end_time)
VALUES
  (1, 1, 'MONDAY', '18:00:00', '21:00:00'),
  (2, 1, 'TUESDAY', '18:00:00', '21:00:00')
ON CONFLICT (id) DO NOTHING;

-- Default fixed event for alice
INSERT INTO events (id, user_id, title, day, start_time, end_time)
VALUES
  (1, 1, 'Lecture', 'TUESDAY', '19:00:00', '20:00:00')
ON CONFLICT (id) DO NOTHING;
