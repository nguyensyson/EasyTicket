CREATE TABLE locations (
    id          CHAR(36)                       NOT NULL DEFAULT (UUID()),
    name        VARCHAR(100)                   NOT NULL,
    delete_flag ENUM('ACTIVE', 'DELETED')      NOT NULL DEFAULT 'ACTIVE',
    created_by  VARCHAR(255)                   NULL,
    created_at  TIMESTAMP                      NULL,
    updated_by  VARCHAR(255)                   NULL,
    updated_at  TIMESTAMP                      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_locations_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO locations (id, name) VALUES
    (UUID(), 'Hà Nội'),
    (UUID(), 'TP. Hồ Chí Minh'),
    (UUID(), 'Đà Nẵng'),
    (UUID(), 'Hải Phòng'),
    (UUID(), 'Cần Thơ');
