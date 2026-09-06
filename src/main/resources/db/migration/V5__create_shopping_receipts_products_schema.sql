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
            ON DELETE SET NULL,
    CONSTRAINT fk_shopping_receipt_space
        FOREIGN KEY (space_id)
            REFERENCES spaces (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_shopping_receipt_image
        FOREIGN KEY (receipt_image_id)
            REFERENCES receipt_images (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_shopping_receipts_creator_id ON shopping_receipts(creator_id);
CREATE INDEX idx_shopping_receipts_space_id ON shopping_receipts(space_id);


CREATE TABLE products
(
    id                        UUID PRIMARY KEY,
    expiration_date           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    suggested_storage_spot_id UUID,
    actual_storage_spot_id    UUID NOT NULL,
    product_type              VARCHAR(30) NOT NULL,
    shopping_receipt_id       UUID,
    price                     NUMERIC(6, 2) NOT NULL,
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
                                                ('FRUITS', 'VEGETABLES', 'OTHER_FRESH_PRODUCTS', 'MEAT', 'SEAFOOD',
                                                 'DAIRY',
                                                 'DELI', 'BAKERY', 'PANTRY', 'SNACKS', 'SWEETS',
                                                 'FROZEN_FOODS',
                                                 'ICE_CREAM_AND_DESSERTS', 'BEVERAGES', 'INTERNATIONAL', 'SAUCES',
                                                 'OTHER'))
);

CREATE INDEX idx_shopping_receipts_actual_storage_spot_id ON shopping_receipts(actual_storage_spot_id);


CREATE TABLE product_storage_spot_history
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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

CREATE INDEX product_storage_spot_history_product_id ON product_storage_spot_history(product_id);