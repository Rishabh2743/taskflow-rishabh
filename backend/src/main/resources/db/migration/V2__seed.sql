-- Insert test user
INSERT INTO users (id, name, email, password)
VALUES (
    uuid_generate_v4(),
    'Test User',
    'test@example.com',
    '$2a$12$u1jKp5J9bZK9cFh1h1h1hOQqQqQqQqQqQqQqQqQqQqQqQqQqQqQq'
);

-- Insert project
INSERT INTO projects (id, name, description, owner_id)
SELECT 
    uuid_generate_v4(),
    'Demo Project',
    'Test project',
    id
FROM users
WHERE email = 'test@example.com';

-- Insert tasks
INSERT INTO tasks (title, status, priority, project_id)
SELECT 'Task 1', 'todo', 'low', p.id FROM projects p LIMIT 1;

INSERT INTO tasks (title, status, priority, project_id)
SELECT 'Task 2', 'in_progress', 'medium', p.id FROM projects p LIMIT 1;

INSERT INTO tasks (title, status, priority, project_id)
SELECT 'Task 3', 'done', 'high', p.id FROM projects p LIMIT 1;