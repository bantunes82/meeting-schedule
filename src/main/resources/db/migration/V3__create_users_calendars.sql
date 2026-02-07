-- Seed users and their calendars for development

INSERT INTO users (id, name, email) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alice Johnson', 'alice@example.com'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Bob Smith', 'bob@example.com'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Charlie Brown', 'charlie@example.com');

INSERT INTO calendars (id, user_id) VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33');
