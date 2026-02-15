INSERT INTO roles (name) VALUES ('basic');

INSERT INTO scopes (name) VALUES ('tweets');
INSERT INTO scopes (name) VALUES ('users');

INSERT INTO permitted_actions (action) VALUES ('tweet:read');
INSERT INTO permitted_actions (action) VALUES ('tweet:write');
INSERT INTO permitted_actions (action) VALUES ('user:read');
INSERT INTO permitted_actions (action) VALUES ('user:write');

INSERT INTO roles_to_scopes (role_id, scope_id)
SELECT r.id, s.id
FROM roles r
JOIN scopes s ON s.name IN ('tweets', 'users')
WHERE r.name = 'basic';

INSERT INTO scopes_to_permitted_actions (scope_id, permitted_action_id)
SELECT s.id, p.id
FROM scopes s
JOIN permitted_actions p
    ON (s.name = 'tweets' AND p.action IN ('tweet:read', 'tweet:write'))
    OR (s.name = 'users' AND p.action IN ('user:read', 'user:write'));
