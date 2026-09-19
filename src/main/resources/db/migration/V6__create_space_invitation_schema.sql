CREATE TABLE space_invitation
(
    id              UUID PRIMARY KEY,
    token           VARCHAR(252) UNIQUE NOT NULL,
    space_id        UUID NOT NULL,
    created_by      UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    max_uses        INT,
    uses_count      INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_space_invitation_space_id
        FOREIGN KEY (space_id)
            REFERENCES spaces (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_space_invitation_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id)
            ON DELETE CASCADE
);
