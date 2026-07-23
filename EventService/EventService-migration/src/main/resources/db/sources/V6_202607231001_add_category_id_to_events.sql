ALTER TABLE events
    ADD COLUMN category_id CHAR(36) NULL AFTER description;

UPDATE events e
    JOIN categories c ON c.name = CASE e.category
        WHEN 'MUSIC'      THEN 'Nhạc sống'
        WHEN 'SPORTS'     THEN 'Thể thao'
        WHEN 'WORKSHOP'   THEN 'Hội thảo'
        WHEN 'THEATER'    THEN 'Sân khấu & Nghệ thuật'
        WHEN 'CONFERENCE' THEN 'Hội nghị'
        ELSE 'Khác'
    END
    SET e.category_id = c.id,
        e.category = c.name;

ALTER TABLE events
    MODIFY COLUMN category_id CHAR(36) NOT NULL,
    ADD CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories (id),
    ADD KEY idx_events_category_id (category_id);
