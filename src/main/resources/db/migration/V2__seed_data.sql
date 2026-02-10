INSERT INTO users (first_name, last_name, email, handle)
VALUES ('Ignacio', 'Gomez', 'ignacio@gomez.com', 'nachogomez');

INSERT INTO tweets (content, user_id, timestamp)
SELECT 'hello', id, NOW()
FROM users WHERE handle = 'nachogomez';
