-- Sample data crawled from ticketbox.vn: adds the category found in the crawled events
-- that isn't covered by the existing seed set (V5) - exhibitions/trade fairs.
--
-- Also defensively re-seeds V5's base categories (idempotent via WHERE NOT EXISTS): on this
-- environment the categories table exists but V5's INSERT never actually landed any rows,
-- so events referencing those categories would silently match zero rows and never insert.
INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Nhạc sống') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Nhạc sống');

INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Sân khấu & Nghệ thuật') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Sân khấu & Nghệ thuật');

INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Thể thao') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thể thao');

INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Hội thảo') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hội thảo');

INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Hội nghị') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hội nghị');

INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Khác') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Khác');

INSERT INTO categories (id, name)
SELECT * FROM (SELECT UUID(), 'Triển lãm & Hội chợ') AS v(id, name)
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Triển lãm & Hội chợ');
