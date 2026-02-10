CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    handle VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE tweets (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(200) NOT NULL,
    timestamp TIMESTAMP,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_tweets_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_tweets_user_id ON tweets(user_id);
CREATE INDEX idx_tweets_timestamp ON tweets(timestamp DESC);
