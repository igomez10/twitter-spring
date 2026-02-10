INSERT INTO scopes (name) VALUES ('users') ON CONFLICT DO NOTHING;
INSERT INTO permitted_actions (action) VALUES ('user:write') ON CONFLICT DO NOTHING;

INSERT INTO roles_to_scopes (role_id, scope_id)
SELECT r.id, s.id
FROM roles r, scopes s
WHERE r.name = 'basic' AND s.name = 'users'
ON CONFLICT DO NOTHING;

INSERT INTO scopes_to_permitted_actions (scope_id, permitted_action_id)
SELECT s.id, p.id
FROM scopes s
JOIN permitted_actions p ON p.action IN ('user:read', 'user:write')
WHERE s.name = 'users'
ON CONFLICT DO NOTHING;
