CREATE TABLE notifications (
    id           CHAR(36)                              NOT NULL DEFAULT (UUID()),
    order_id     CHAR(36)                              NOT NULL,
    type         ENUM('TICKET_QR', 'ORDER_CANCELLED')  NOT NULL,
    channel      ENUM('EMAIL')                         NOT NULL DEFAULT 'EMAIL',
    status       ENUM('QUEUED', 'QUEUE_FAILED')        NOT NULL,
    error_message VARCHAR(500)                         NULL,
    queued_at    DATETIME                              NULL,
    delete_flag  ENUM('ACTIVE', 'DELETED')             NOT NULL DEFAULT 'ACTIVE',
    created_by   VARCHAR(255)                          NULL,
    created_at   TIMESTAMP                             NULL,
    updated_by   VARCHAR(255)                          NULL,
    updated_at   TIMESTAMP                             NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_notifications_order_id_type (order_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
