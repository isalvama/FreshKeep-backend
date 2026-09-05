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

CREATE TABLE receipt_images
(
    id         UUID PRIMARY KEY,
    asset_id   VARCHAR(252) UNIQUE NOT NULL,
    mime_type  VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE spaces
(
    id              UUID PRIMARY KEY,
    name            VARCHAR(30) NOT NULL,
    emoji           VARCHAR(8) NOT NULL,
    creator_id      UUID,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spaces_creator
        FOREIGN KEY (creator_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE TABLE storage_spots
(
    id                UUID PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    storage_spot_type VARCHAR(30) NOT NULL,
    space_id          UUID NOT NULL,
    CONSTRAINT fk_storage_spots_space
        FOREIGN KEY (space_id)
            REFERENCES spaces (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_storage_spots_storage_spot_type CHECK (storage_spot_type IN
                                                          ('FRIDGE', 'FREEZER', 'PANTRY', 'FRUIT_BOWL', 'WINE_CELLAR',
                                                           'COUNTERTOP', 'SHELF')),
    CONSTRAINT uq_space_spot_name UNIQUE (space_id, name)
);

CREATE TABLE spaces_participants
(
    participant_id UUID,
    space_id       UUID NOT NULL,
    CONSTRAINT fk_spaces_participants_participant
        FOREIGN KEY (participant_id)
            REFERENCES users (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_spaces_participants_space
        FOREIGN KEY (space_id)
            REFERENCES spaces (id)
            ON DELETE CASCADE
);
