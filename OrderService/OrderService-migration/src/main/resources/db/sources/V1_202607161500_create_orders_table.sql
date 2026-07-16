CREATE TABLE orders (
    id             CHAR(36)                                          NOT NULL DEFAULT (UUID()),
    reservation_id VARCHAR(255)                                      NOT NULL,
    user_id        VARCHAR(255)                                      NOT NULL,
    event_id       CHAR(36)                                          NOT NULL,
    ticket_type_id CHAR(36)                                          NOT NULL,
    quantity       INT                                               NOT NULL,
    unit_price     DECIMAL(12, 2)                                    NOT NULL,
    total_amount   DECIMAL(12, 2)                                    NOT NULL,
    payment_id     CHAR(36)                                          NULL,
    status         ENUM('PENDING_PAYMENT', 'PAID', 'CANCELLED')      NOT NULL DEFAULT 'PENDING_PAYMENT',
    delete_flag    ENUM('ACTIVE', 'DELETED')                         NOT NULL DEFAULT 'ACTIVE',
    created_by     VARCHAR(255)                                      NULL,
    created_at     TIMESTAMP                                         NULL,
    updated_by     VARCHAR(255)                                      NULL,
    updated_at     TIMESTAMP                                         NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_orders_reservation_id (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
