CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_log_in TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE role (
    account_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    CONSTRAINT pk_account_roles PRIMARY KEY (account_id, role),
    CONSTRAINT fk_account_roles_account
         FOREIGN KEY (account_id)
             REFERENCES accounts (id)
             ON DELETE CASCADE,
    CONSTRAINT chk_account_roles_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_accounts_email ON accounts(email);
