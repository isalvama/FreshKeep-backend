CREATE TABLE users
(
    id              UUID PRIMARY KEY,
    account_id      UUID UNIQUE NOT NULL,
    email           VARCHAR(40) UNIQUE NOT NULL,
    username        VARCHAR(20) UNIQUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_user
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_users_email ON users(email);