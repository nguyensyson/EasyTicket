CREATE TABLE user_profiles (
    id          CHAR(36)                       NOT NULL DEFAULT (UUID()),
    full_name   VARCHAR(100)                   NOT NULL,
    phone       VARCHAR(20)                    NULL,
    avatar_url  VARCHAR(255)                   NULL,
    address     VARCHAR(255)                   NULL,
    delete_flag ENUM('ACTIVE', 'DELETED')      NOT NULL DEFAULT 'ACTIVE',
    created_by  VARCHAR(255)                   NULL,
    created_at  TIMESTAMP                      NULL,
    updated_by  VARCHAR(255)                   NULL,
    updated_at  TIMESTAMP                      NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
