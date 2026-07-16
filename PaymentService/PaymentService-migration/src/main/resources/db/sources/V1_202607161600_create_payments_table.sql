CREATE TABLE payments (
    id                      CHAR(36)                                          NOT NULL DEFAULT (UUID()),
    order_id                CHAR(36)                                          NOT NULL,
    reservation_id          VARCHAR(255)                                      NULL,
    user_id                 VARCHAR(255)                                      NOT NULL,
    amount                  DECIMAL(12, 2)                                    NOT NULL,
    payment_method          ENUM('CARD', 'MOMO', 'VNPAY', 'BANK_TRANSFER')    NOT NULL,
    status                  ENUM('PENDING', 'SUCCESS', 'FAILED')              NOT NULL DEFAULT 'PENDING',
    external_transaction_id VARCHAR(255)                                      NULL,
    expires_at              DATETIME                                         NOT NULL,
    failed_reason           ENUM('DECLINED', 'TIMEOUT')                       NULL,
    delete_flag             ENUM('ACTIVE', 'DELETED')                         NOT NULL DEFAULT 'ACTIVE',
    created_by              VARCHAR(255)                                      NULL,
    created_at              TIMESTAMP                                         NULL,
    updated_by              VARCHAR(255)                                      NULL,
    updated_at               TIMESTAMP                                        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payments_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
