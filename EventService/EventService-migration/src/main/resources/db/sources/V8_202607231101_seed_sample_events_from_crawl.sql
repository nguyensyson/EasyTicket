-- Sample event data crawled from ticketbox.vn, one event per organizer created in
-- V7/UserService (organizer_id = the organizer's Keycloak user id).

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '88d1b145-9236-4412-80b4-5a39cde2c41b',
    'THE GENTLEMEN - COUNTDOWN CONCERT 2026',
    'Đêm nhạc countdown chào năm mới 2026 tại The Pearl Hoi An, Đà Nẵng.',
    c.id, c.name,
    l.id, 'The Pearl Hoi An, Khối An Bàng, Phường Hội An Tây, Thành Phố Đà Nẵng',
    'https://salt.tkbcdn.com/ts/ds/27/b5/52/1b92d99147733d76b376b207dc45595f.jpg',
    '2025-12-31 19:30:00', '2025-12-31 23:59:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Nhạc sống' AND l.name = 'Đà Nẵng';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '5f900498-ce6f-4896-aad6-fae95b8d2343',
    'Liveshow Góc Ban Công: Vệt nắng - TUẤN HƯNG, QUẢ DƯA HẤU, LỆ QUYÊN, PHÚC TIỆP, ĐĂNG KHÔI, HÀ ANH,...',
    'Liveshow quy tụ Tuấn Hưng, Quả Dưa Hấu, Lệ Quyên, Phúc Tiệp, Đăng Khôi, Hà Anh và nhiều nghệ sĩ khách mời tại Hà Nội.',
    c.id, c.name,
    l.id, 'Hội trường Trung tâm Văn hoá Thể thao Quần Ngựa, số 55, Đốc Ngữ, Phường Ngọc Hà, Thành phố Hà Nội',
    'https://salt.tkbcdn.com/ts/ds/52/96/35/b73cf6db01fa3541951377c518182f15.jpg',
    '2026-06-13 20:00:00', '2026-06-13 22:00:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Nhạc sống' AND l.name = 'Hà Nội';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '7e9e7df3-3147-4c29-b82f-9b107a01bf96',
    '[Hà Nội] Những Thành Phố Mơ Màng Summer 2026',
    'Lễ hội âm nhạc ngoài trời mùa hè quy tụ nhiều nghệ sĩ đa phong cách tại Công viên Yên Sở, Hà Nội.',
    c.id, c.name,
    l.id, 'Công Viên Yên Sở (ngoài trời), QL1A Gamuda Central, Phường Yên Sở, Thành phố Hà Nội',
    'https://salt.tkbcdn.com/ts/ds/fc/7c/69/38fc8dec7d099ab1ec1c62df12107ddd.png',
    '2026-07-12 16:00:00', '2026-07-12 22:30:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Nhạc sống' AND l.name = 'Hà Nội';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    'f6c966a5-d647-40fd-9790-2689e6964d83',
    '[Hà Nội] Triển lãm & Lễ hội Quốc tế Thú cưng Việt Nam - InterPet Expo & InterPetFest Việt Nam 2026',
    'Triển lãm và lễ hội quốc tế thú cưng tại Hà Nội, quy tụ các thương hiệu vật nuôi trong và ngoài nước.',
    c.id, c.name,
    l.id, 'Cung Triển lãm Xây dựng Hà Nội - NECC, 01 Đỗ Đức Dục, phường Từ Liêm, Hà Nội',
    'https://salt.tkbcdn.com/ts/ds/92/13/7b/08fc776554c54fc041bd8be70d94f0fb.png',
    '2025-02-20 09:00:00', '2025-02-22 19:00:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Triển lãm & Hội chợ' AND l.name = 'Hà Nội';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '02ce74cf-957b-41ea-8507-f5199102082d',
    'Vietnam Int''l Cafe Show 2026 in HCMC',
    'Triển lãm quốc tế ngành cà phê tại TP. Hồ Chí Minh, do Exporum Vietnam tổ chức.',
    c.id, c.name,
    l.id, 'Sảnh A, Trung tâm Hội chợ và Triển lãm Sài Gòn (SECC), 799 Nguyễn Văn Linh, Phường Tân Phú, Quận 7, Thành Phố Hồ Chí Minh',
    'https://salt.tkbcdn.com/ts/ds/45/25/5e/4a1f7d0b0b204518b3a7918eba93fcfd.jpg',
    '2026-04-16 09:00:00', '2026-04-18 17:00:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Triển lãm & Hội chợ' AND l.name = 'TP. Hồ Chí Minh';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '08dde8b9-428c-48de-9c99-36ddbbab0193',
    'Lion Championship 30 - 2026',
    'Giải đấu võ thuật tổng hợp Lion Championship mùa giải thứ 30 tại Hà Nội.',
    c.id, c.name,
    l.id, 'Nhà thi đấu Tây Hồ, 101 Xuân La, Phường Tây Hồ, Thành phố Hà Nội',
    'https://salt.tkbcdn.com/ts/ds/81/ef/bb/35cdf7f9ce25b178d5abdcadba2a5bdb.jpg',
    '2026-04-18 20:00:00', '2026-04-18 22:30:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Thể thao' AND l.name = 'Hà Nội';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '08dde8b9-428c-48de-9c99-36ddbbab0193',
    'Lion Championship 32 - 2026',
    'Giải đấu võ thuật tổng hợp Lion Championship mùa giải thứ 32 tại Hà Nội.',
    c.id, c.name,
    l.id, 'Nhà thi đấu Xuân Đỉnh, 101 Xuân La, Phường Xuân Đỉnh, Thành phố Hà Nội',
    'https://salt.tkbcdn.com/ts/ds/97/d3/75/83da93a5ed83413fab49d2a3cedde35c.jpg',
    '2026-06-06 20:00:00', '2026-06-06 23:00:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Thể thao' AND l.name = 'Hà Nội';

INSERT INTO events (organizer_id, title, description, category_id, category, location_id, location, banner_url, start_time, end_time, status)
SELECT
    '0d1acda3-20ee-4c8c-9954-6d58cf46e271',
    'PPA ASIA 1000 - MB HANOI CUP 2026',
    'Giải đấu Pickleball chuyên nghiệp PPA Asia 1000 - MB Hanoi Cup tại Hà Nội.',
    c.id, c.name,
    l.id, 'Cung Điền kinh Mỹ Đình, Phố Trần Hữu Dực, Nam Từ Liêm, Hà Nội',
    'https://salt.tkbcdn.com/ts/ds/66/c4/1a/829f6d0533407aaa5a134c541e6f9bb9.png',
    '2026-04-05 08:00:00', '2026-04-05 22:00:00',
    'PUBLISHED'
FROM categories c, locations l
WHERE c.name = 'Thể thao' AND l.name = 'Hà Nội';
