# Demo Organizer Accounts

Tài khoản Organizer mẫu dùng cho demo/dev local, được tạo qua API `POST /api/v1/users/register/organizer` (UserService, Keycloak realm `EasyTicket`). Tên tổ chức và dữ liệu sự kiện tương ứng được crawl từ [ticketbox.vn](https://ticketbox.vn) ngày 2026-07-24.

> ⚠️ Chỉ dùng cho môi trường local/dev. Không dùng lại các mật khẩu này cho môi trường khác.

| Username | Password | Email | Họ tên (Organizer) | User ID (Keycloak / `user_profiles.id`) |
|---|---|---|---|---|
| `thepearlhoian` | `J3mHdvwx#67y` | thepearlhoian@organizer.easyticket.demo | The Pearl Hoi An | `88d1b145-9236-4412-80b4-5a39cde2c41b` |
| `absolutemedia` | `RKtqnhdq%429` | absolutemedia@organizer.easyticket.demo | Absolute Media | `5f900498-ce6f-4896-aad6-fae95b8d2343` |
| `nhungthanhphomomang` | `mqHMmehz982%` | nhungthanhphomomang@organizer.easyticket.demo | Những Thành Phố Mơ Màng | `7e9e7df3-3147-4c29-b82f-9b107a01bf96` |
| `eventure` | `EJ7a9qqvy!4e` | eventure@organizer.easyticket.demo | Eventure | `f6c966a5-d647-40fd-9790-2689e6964d83` |
| `exporumvietnam` | `uEnL3st!d6b5` | exporumvietnam@organizer.easyticket.demo | Exporum Vietnam | `02ce74cf-957b-41ea-8507-f5199102082d` |
| `vothuattonghopvn` | `DT4jrcb2*p9t` | vothuattonghopvn@organizer.easyticket.demo | Công ty Cổ phần Võ thuật Tổng hợp Việt Nam | `08dde8b9-428c-48de-9c99-36ddbbab0193` |
| `upaasianewsports` | `GYa7mvntd97!` | upaasianewsports@organizer.easyticket.demo | UPA Asia & New Sports | `0d1acda3-20ee-4c8c-9954-6d58cf46e271` |

Mỗi Organizer ở trên có đúng 1 sự kiện mẫu tương ứng, xem `EventService/EventService-migration/src/main/resources/db/sources/V8_202607231101_seed_sample_events_from_crawl.sql` (`organizer_id` = User ID ở bảng trên).

## Ghi chú

- Login: `POST /api/v1/users/login` với `username`/`password` ở trên.
- Category mẫu bổ sung "Triển lãm & Hội chợ" (cho các sự kiện expo/hội chợ crawl được, không nằm trong 6 category gốc) nằm trong `V7_202607231100_seed_categories_from_crawl.sql`.
