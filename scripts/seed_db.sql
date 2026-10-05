CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(100) PRIMARY KEY,
    preferences JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS items (
    item_id VARCHAR(100) PRIMARY KEY,
    categories TEXT[] NOT NULL,
    features JSONB DEFAULT '{}'::jsonb
);

INSERT INTO users (user_id, preferences) VALUES
('user001', '{"tech": 3, "books": 1}'::jsonb),
('user002', '{"sports": 3}'::jsonb),
('user003', '{"travel": 2, "food": 1}'::jsonb),
('user004', '{"music": 3}'::jsonb),
('user005', '{"fashion": 2}'::jsonb),
('user006', '{}'::jsonb),
('user007', '{"tech": 1}'::jsonb),
('user008', '{"fitness": 2}'::jsonb),
('user009', '{"books": 2}'::jsonb),
('user010', '{"food": 2}'::jsonb)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO items (item_id, categories, features) VALUES
('item001', ARRAY['tech'], '{"title":"Java Backend Guide","type":"article"}'),
('item002', ARRAY['tech','books'], '{"title":"Spring Boot Patterns","type":"book"}'),
('item003', ARRAY['tech'], '{"title":"Kafka Fundamentals","type":"article"}'),
('item004', ARRAY['tech','cloud'], '{"title":"Cloud Architecture","type":"course"}'),
('item005', ARRAY['sports'], '{"title":"Football Highlights","type":"video"}'),
('item006', ARRAY['sports','fitness'], '{"title":"Training Basics","type":"article"}'),
('item007', ARRAY['travel'], '{"title":"Kerala Travel Guide","type":"guide"}'),
('item008', ARRAY['travel','food'], '{"title":"Food Trails","type":"guide"}'),
('item009', ARRAY['music'], '{"title":"Music Trends","type":"article"}'),
('item010', ARRAY['music'], '{"title":"Guitar Essentials","type":"course"}'),
('item011', ARRAY['fashion'], '{"title":"Summer Fashion","type":"article"}'),
('item012', ARRAY['fashion','shopping'], '{"title":"Style Guide","type":"guide"}'),
('item013', ARRAY['fitness'], '{"title":"Home Workout","type":"video"}'),
('item014', ARRAY['fitness','health'], '{"title":"Running Plan","type":"article"}'),
('item015', ARRAY['books'], '{"title":"Modern Fiction","type":"book"}'),
('item016', ARRAY['books','history'], '{"title":"History Classics","type":"book"}'),
('item017', ARRAY['food'], '{"title":"Indian Recipes","type":"recipe"}'),
('item018', ARRAY['food','travel'], '{"title":"Street Food","type":"video"}'),
('item019', ARRAY['cloud','tech'], '{"title":"Docker Deep Dive","type":"course"}'),
('item020', ARRAY['sports','fitness'], '{"title":"Sports Science","type":"article"}')
ON CONFLICT (item_id) DO NOTHING;
