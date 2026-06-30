CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(64) NOT NULL UNIQUE,
    margin_pct  NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE sessions (
    token       VARCHAR(128) PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    expires_at  TIMESTAMP NOT NULL
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);

INSERT INTO users (username, margin_pct) VALUES
    ('alice', 0.00),
    ('bob', 5.00),
    ('carol', 12.50)
ON CONFLICT (username) DO NOTHING;
