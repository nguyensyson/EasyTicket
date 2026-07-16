CREATE TABLE ticket_types (
    id             CHAR(36)                  NOT NULL DEFAULT (UUID()),
    event_id       CHAR(36)                  NOT NULL,
    name           VARCHAR(100)              NOT NULL,
    price          DECIMAL(12, 2)            NOT NULL,
    total_quantity INT                       NOT NULL,
    delete_flag    ENUM('ACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    created_by     VARCHAR(255)              NULL,
    created_at     TIMESTAMP                 NULL,
    updated_by     VARCHAR(255)              NULL,
    updated_at     TIMESTAMP                 NULL,
    PRIMARY KEY (id),
    KEY idx_ticket_types_event_id (event_id),
    CONSTRAINT fk_ticket_types_event FOREIGN KEY (event_id) REFERENCES events (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
