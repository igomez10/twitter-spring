CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE scopes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE permitted_actions (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE roles_to_scopes (
    role_id BIGINT NOT NULL,
    scope_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, scope_id),
    CONSTRAINT fk_roles_to_scopes_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_to_scopes_scope FOREIGN KEY (scope_id) REFERENCES scopes(id) ON DELETE CASCADE
);

CREATE TABLE scopes_to_permitted_actions (
    scope_id BIGINT NOT NULL,
    permitted_action_id BIGINT NOT NULL,
    PRIMARY KEY (scope_id, permitted_action_id),
    CONSTRAINT fk_scopes_to_permitted_actions_scope FOREIGN KEY (scope_id) REFERENCES scopes(id) ON DELETE CASCADE,
    CONSTRAINT fk_scopes_to_permitted_actions_action FOREIGN KEY (permitted_action_id) REFERENCES permitted_actions(id) ON DELETE CASCADE
);

CREATE TABLE user_to_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_to_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_to_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE user_credentials (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_roles_to_scopes_scope_id ON roles_to_scopes(scope_id);
CREATE INDEX idx_scopes_to_permitted_actions_action_id ON scopes_to_permitted_actions(permitted_action_id);
CREATE INDEX idx_user_to_roles_role_id ON user_to_roles(role_id);
