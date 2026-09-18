CREATE TABLE shopping_receipts
(
    id               UUID PRIMARY KEY,
    creator_id       UUID,
    space_id         UUID,
    receipt_image_id UUID UNIQUE,
    purchase_date    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    store_name       VARCHAR(255),
    status           VARCHAR(30) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
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
            ON DELETE SET NULL,
    CONSTRAINT chk_shopping_receipts_status CHECK (status IN
            ('DRAFT', 'CONFIRMED'))
);

CREATE INDEX idx_shopping_receipts_creator_id ON shopping_receipts(creator_id);
CREATE INDEX idx_shopping_receipts_space_id ON shopping_receipts(space_id);


CREATE TABLE products
(
    id                        UUID PRIMARY KEY,
    name                      VARCHAR(30) NOT NULL,
    expiration_date           DATE DEFAULT CURRENT_DATE NOT NULL,
    suggested_storage_spot_id UUID,
    actual_storage_spot_id    UUID NOT NULL,
    product_type              VARCHAR(30) NOT NULL,
    shopping_receipt_id       UUID,
    price                     NUMERIC(10, 2),
    currency                  VARCHAR(20),
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at       TIMESTAMP WITH TIME ZONE,
    version                   BIGINT NOT NULL DEFAULT 0,

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
                                                 'OTHER')),

        CONSTRAINT chk_products_currency CHECK (currency IN (
        'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD',
        'SEK', 'NOK', 'DKK', 'PLN', 'CZK', 'HUF', 'RON', 'BGN',
        'MXN', 'BRL', 'ARS', 'CLP', 'COP', 'PEN', 'UYU',
        'CNY', 'HKD', 'SGD', 'INR', 'KRW', 'THB', 'IDR', 'MYR', 'PHP', 'VND',
        'AED', 'SAR', 'ILS', 'TRY', 'ZAR', 'EGP', 'NGN',
        'UAH', 'RUB'
        ))
);

CREATE INDEX idx_products_actual_storage_spot_id ON products(actual_storage_spot_id);
CREATE INDEX idx_products_shopping_receipt_id ON products(shopping_receipt_id);


CREATE SEQUENCE product_storage_spot_history_seq START WITH 1 INCREMENT BY 30;

CREATE TABLE product_storage_spot_history
(
    id                  BIGINT PRIMARY KEY DEFAULT nextval('product_storage_spot_history_seq'),
    product_id          UUID NOT NULL,
    user_id             UUID,
    new_expiration_date DATE NOT NULL,
    new_storage_spot_id UUID,
    changed_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
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