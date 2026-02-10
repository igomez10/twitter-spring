INSERT INTO roles (name) VALUES ('basic');
INSERT INTO scopes (name) VALUES ('tweets');
INSERT INTO permitted_actions (action) VALUES ('tweet:read');
INSERT INTO permitted_actions (action) VALUES ('tweet:write');
INSERT INTO permitted_actions (action) VALUES ('user:read');

INSERT INTO roles_to_scopes (role_id, scope_id)
SELECT r.id, s.id
FROM roles r, scopes s
WHERE r.name = 'basic' AND s.name = 'tweets';

INSERT INTO scopes_to_permitted_actions (scope_id, permitted_action_id)
SELECT s.id, p.id
FROM scopes s
JOIN permitted_actions p ON p.action IN ('tweet:read', 'tweet:write', 'user:read')
WHERE s.name = 'tweets';

INSERT INTO user_to_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.handle = 'nachogomez' AND r.name = 'basic';
