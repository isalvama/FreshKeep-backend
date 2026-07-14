CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_log_in TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) UNIQUE NOT NULL,
    token_type VARCHAR(20) NOT NULL DEFAULT 'BEARER',
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    account_id UUID NOT NULL,

    CONSTRAINT fk_token_user
         FOREIGN KEY (user_id)
             REFERENCES accounts (id)
             ON DELETE CASCADE
);

CREATE INDEX idx_tokens_token ON tokens(token);
CREATE INDEX idx_tokens_user_id ON tokens(user_id);
