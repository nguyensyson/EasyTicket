CREATE TABLE categories (
    id          CHAR(36)                       NOT NULL DEFAULT (UUID()),
    name        VARCHAR(100)                   NOT NULL,
    delete_flag ENUM('ACTIVE', 'DELETED')      NOT NULL DEFAULT 'ACTIVE',
    created_by  VARCHAR(255)                   NULL,
    created_at  TIMESTAMP                      NULL,
    updated_by  VARCHAR(255)                   NULL,
    updated_at  TIMESTAMP                      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories (id, name) VALUES
    (UUID(), 'Nhạc sống'),
    (UUID(), 'Sân khấu & Nghệ thuật'),
    (UUID(), 'Thể thao'),
    (UUID(), 'Hội thảo'),
    (UUID(), 'Hội nghị'),
    (UUID(), 'Khác');
