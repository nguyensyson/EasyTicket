---
inclusion: manual
---

# EasyTicket – Changelog / Lịch sử Thay đổi

> Mỗi khi có thay đổi đáng kể trong codebase (thêm tính năng, sửa lỗi, refactor, thay đổi cấu hình, cập nhật dependency...), bạn PHẢI ghi lại vào file này theo đúng format bên dưới.
>
> **Quy tắc bắt buộc:**
> - Ghi theo thứ tự **mới nhất lên trên**.
> - Mỗi entry phải có: ngày giờ, loại thay đổi, mô tả, file/module bị ảnh hưởng.
> - Không xóa lịch sử cũ – chỉ thêm vào.

---

## Format Entry

```
### [YYYY-MM-DD HH:mm] – <Loại thay đổi> – <Tiêu đề ngắn>

**Service/Module:** <tên service hoặc module>
**Loại:** FEATURE | BUGFIX | REFACTOR | CONFIG | MIGRATION | DEPENDENCY | SECURITY | DOCS
**Mô tả:**
<Mô tả chi tiết những gì đã thay đổi, lý do thay đổi, tác động>

**Files thay đổi:**
- `path/to/file1.java` – <lý do>
- `path/to/file2.yaml` – <lý do>
```

---

## Lịch sử Thay đổi

### [2026-06-28 12:00] – FEATURE – Tạo Spec (Requirements + Design) cho UserService

**Service/Module:** `UserService`
**Loại:** FEATURE
**Mô tả:**
Tạo toàn bộ tài liệu spec cho UserService – microservice mới trong hệ thống EasyTicket đóng vai trò trung gian với Keycloak và quản lý profile người dùng. Bao gồm tài liệu Requirements (15 yêu cầu EARS-format, phủ toàn bộ login/register/profile/aggregation/security/migration/observability) và tài liệu Design (kiến trúc Maven multi-module 6 module, class design, sequence diagram, DDL MySQL, cấu hình đầy đủ, 12 correctness properties). Database được xác nhận dùng MySQL.

**Files thay đổi:**
- `.kiro/specs/user-service/requirements.md` – Tạo mới (15 requirements, EARS format, MySQL confirmed)
- `.kiro/specs/user-service/design.md` – Tạo mới (high-level design, class design, sequence diagrams, DDL, config, PBT properties)
- `.kiro/specs/user-service/.config.kiro` – Tạo mới (specType: feature, workflowType: requirements-first)

---

### [2026-06-28 00:00] – DOCS – Khởi tạo Changelog Steering File

**Service/Module:** `.kiro/steering`
**Loại:** DOCS
**Mô tả:**
Tạo steering file `changelog.md` để theo dõi lịch sử thay đổi toàn bộ dự án EasyTicket. File này sẽ được cập nhật thủ công (inclusion: manual) mỗi khi có thay đổi đáng kể.

**Files thay đổi:**
- `.kiro/steering/changelog.md` – Tạo mới

---

<!-- Thêm entry mới ở trên dòng này -->
