CREATE TABLE users
(
    id              UUID PRIMARY KEY,
    account_id      UUID UNIQUE NOT NULL,
    username        VARCHAR(30),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_user
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE
);

CREATE TABLE receipt_images
(
    id         UUID PRIMARY KEY,
    image_url  VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
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
                                                           'COUNTERTOP', 'SHELF'))
        ADD CONSTRAINT uq_space_spot_name UNIQUE (space_id, name);
);

CREATE TABLE spaces
(
    id              UUID PRIMARY KEY,
    name            VARCHAR(30) NOT NULL,
    creator_id      UUID,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spaces_creator
        FOREIGN KEY (creator_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);


CREATE TABLE shopping_receipts
(
    id               UUID PRIMARY KEY,
    creator_id       UUID,
    space_id         UUID,
    receipt_image_id UUID UNIQUE,
    purchase_date    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    store_name       VARCHAR(255) NOT NULL,
    CONSTRAINT fk_shopping_receipt_creator
        FOREIGN KEY (creator_id)
            REFERENCES users (id)
            ON DELETE SET NULL
    CONSTRAINT fk_shopping_receipt_space
        FOREIGN KEY (space_id)
            REFERENCES spaces (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_shopping_receipt_image
        FOREIGN KEY (receipt_image_id)
            REFERENCES receipt_images (id)
            ON DELETE SET NULL
);

CREATE TABLE products
(
    id                        UUID PRIMARY KEY,
    expiration_date           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    suggested_storage_spot_id UUID,
    actual_storage_spot_id    UUID NOT NULL,
    product_type              VARCHAR(30) NOT NULL,
    shopping_receipt_id       UUID,
    price                     NUMERIC(6, 2) NOT NULL
    CONSTRAINT fk_products_suggested_storage_spot
        FOREIGN KEY (suggested_storage_spot_id)
            REFERENCES storage_spots (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_products_actual_storage_spot
        FOREIGN KEY (actual_storage_spot_id)
            REFERENCES storage_spots (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_products_shopping_receipt
        FOREIGN KEY (shopping_receipt_id)
            REFERENCES shopping_receipts (id)
            ON DELETE SET NULL,
    CONSTRAINT chk_products_product_type CHECK (product_type IN
                                                ('FRUITS', 'VEGETABLES', 'OTHER FRESH PRODUCTS', 'MEAT', 'SEAFOOD',
                                                 'DAIRY',
                                                 'DELI', 'BAKERY', 'VEGETABLES', 'PANTRY', 'SNACKS', 'SWEETS',
                                                 'FROZEN FOODS',
                                                 'ICE CREAM AND DESSERTS', 'BEVERAGES', 'INTERNATIONAL', "SAUCES",
                                                 "OTHER"))
);

CREATE TABLE spaces_participants
(
    id             UUID PRIMARY KEY,
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

CREATE TABLE product_storage_spot_history
(
    id                  UUID PRIMARY KEY,
    product_id          UUID NOT NULL,
    user_id             UUID,
    new_expiration_date TIMESTAMP WITH TIME ZONE NOT NULL,
    new_storage_spot_id UUID,
    change_date         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_storage_spot_history_product
        FOREIGN KEY (product_id)
            REFERENCES products (id),
    CONSTRAINT fk_product_storage_spot_history_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_storage_spot_history_new_storage_spot
        FOREIGN KEY (new_storage_spot_id)
            REFERENCES storage_spots (id)
            ON DELETE SET NULL
);
