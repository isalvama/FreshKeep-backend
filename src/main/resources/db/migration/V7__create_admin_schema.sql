CREATE TABLE admins
(
    id              UUID PRIMARY KEY,
    account_id      UUID UNIQUE NOT NULL,
    email           VARCHAR(40) UNIQUE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_admin
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_admins_email ON admins(email);