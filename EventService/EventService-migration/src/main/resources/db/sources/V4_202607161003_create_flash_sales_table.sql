CREATE TABLE flash_sales (
    id          CHAR(36)                            NOT NULL DEFAULT (UUID()),
    event_id    CHAR(36)                            NOT NULL,
    start_at    DATETIME                            NOT NULL,
    end_at      DATETIME                            NOT NULL,
    status      ENUM('SCHEDULED', 'ACTIVE', 'ENDED') NOT NULL DEFAULT 'SCHEDULED',
    delete_flag ENUM('ACTIVE', 'DELETED')           NOT NULL DEFAULT 'ACTIVE',
    created_by  VARCHAR(255)                        NULL,
    created_at  TIMESTAMP                           NULL,
    updated_by  VARCHAR(255)                        NULL,
    updated_at  TIMESTAMP                           NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flash_sales_event_id (event_id),
    CONSTRAINT fk_flash_sales_event FOREIGN KEY (event_id) REFERENCES events (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
