# EasyTicket — Frontend

Giao diện web đặt vé sự kiện, dựng theo prototype thiết kế tại
`../Ticketbox_website_redesign`. Đây là bản UI độc lập dùng **mock
data** (`src/data/events.ts`) — chưa kết nối các microservice backend
trong repo (Event/Ticket/Order/Payment/User Service).

## Yêu cầu

- **Node.js 20.x** (khuyến nghị dùng `nvm use 20`)
- npm 10+

## Cài đặt

```bash
npm install
```

## Chạy dev server

```bash
npm run dev
```

Mặc định chạy tại `http://localhost:5173`.

## Build production

```bash
npm run build   # type-check (tsc -b) + build (vite build), output ở dist/
npm run preview # xem thử bản build
```

## Kiểm tra code

```bash
npm run lint
```

## Cấu trúc thư mục

```
src/
  assets/            # ảnh, font tĩnh
  components/
    layout/           # Header, Footer, MainLayout (khu vực buyer)
    organizer/         # OrganizerLayout (sidebar), RequireOrganizer, StatCard, StatusBadge
    ui/                # Button, Badge, PillButton — component dùng chung
    events/            # EventCard, HeroCarousel, CategoryTabs, TicketRow
    checkout/          # PaymentOption
  pages/               # Home, EventDetail, Checkout, OrderSuccess, Login, Register, OrderHistory, NotFound
    organizer/          # OrganizerDashboard, OrganizerEvents, OrganizerEventForm, OrganizerEventDashboard
  context/             # CartContext, AuthContext, OrderContext, OrganizerEventContext (state toàn cục, lưu localStorage)
  hooks/               # useCart, useAuth, useOrders, useOrganizerEvents, useAllEvents
  data/                # events.ts (mock seed data), categories.ts
  utils/               # format.ts, eventImage.ts, organizerEventAdapter.ts, organizerStats.ts
  types/               # kiểu dữ liệu dùng chung (EventItem, OrganizerEvent, TicketTypeDef...)
```

## Routes

### Khu vực người mua (`MainLayout`)

| Path | Trang |
|---|---|
| `/` | Trang chủ — hero carousel, lọc danh mục, tìm kiếm, lưới sự kiện |
| `/events/:id` | Chi tiết sự kiện — chọn loại vé |
| `/checkout/:id` | Thanh toán |
| `/order-success/:orderId` | Xác nhận đặt vé thành công |
| `/login`, `/register` | Đăng nhập / đăng ký, có chọn vai trò Buyer/Organizer |
| `/account/orders` | Lịch sử đơn hàng (yêu cầu đăng nhập) |

### Khu vực Organizer (`OrganizerLayout`, yêu cầu role `organizer`)

| Path | Trang |
|---|---|
| `/organizer` | Tổng quan — số sự kiện, tổng vé bán, tổng doanh thu, sự kiện gần đây |
| `/organizer/events` | Danh sách sự kiện của tôi — lọc theo trạng thái, xuất bản/hủy/xóa |
| `/organizer/events/new` | Tạo sự kiện (thông tin, loại vé, Flash Sale) |
| `/organizer/events/:id/edit` | Sửa sự kiện |
| `/organizer/events/:id/dashboard` | Doanh thu & vé bán theo loại vé của 1 sự kiện |

## Nghiệp vụ Organizer (mock, không có backend)

Toàn bộ dữ liệu sự kiện Organizer tạo ra được lưu trong `OrganizerEventContext`
(`localStorage`, key `veluawa_organizer_events`), theo schema bám sát thiết kế
DB thật ở README gốc (`events` / `ticket_types` / `flash_sales`, trạng thái
`DRAFT → PUBLISHED → CANCELLED`, xóa = soft delete qua `deleteFlag`).

- Sự kiện Organizer **đã xuất bản** (`PUBLISHED`) được gộp cùng mock data
  tĩnh (`src/data/events.ts`) để hiển thị trên trang chủ — xem
  `hooks/useAllEvents.ts`. Sự kiện `DRAFT`/`CANCELLED` chỉ Organizer sở hữu
  mới xem được (qua trang quản lý), nhưng vẫn có thể xem trước qua link trực
  tiếp `/events/:id` (không hiển thị công khai trên trang chủ).
- Dashboard doanh thu (`/organizer/events/:id/dashboard`, và số tổng ở
  `/organizer`) được tính **trực tiếp từ các Order thật** đã tạo qua luồng
  checkout (`OrderContext`) — không dùng số liệu giả lập. Nghĩa là nếu một
  buyer thực sự mua vé của một sự kiện do Organizer tạo, doanh thu/số vé bán
  sẽ cập nhật ngay trên dashboard.
- Vì đây là mock cục bộ (không có backend chung), hai "người dùng" muốn thấy
  cùng dữ liệu (Organizer tạo sự kiện, Buyer mua vé) phải dùng **chung một
  trình duyệt** — `localStorage` không chia sẻ được giữa các trình duyệt/thiết
  bị khác nhau. Khi có Event/Order Service thật, dữ liệu sẽ đến từ API dùng
  chung, không còn giới hạn này.
- `/organizer/*` được bảo vệ bởi `RequireOrganizer`: chưa đăng nhập → chuyển
  tới `/login`; đăng nhập với role `buyer` → chuyển về trang chủ.

## Ghi chú quan trọng

- **Chưa có backend thật**: giỏ hàng, đơn hàng, phiên đăng nhập, sự kiện do
  Organizer tạo — tất cả lưu ở `localStorage` trình duyệt, không gọi API.
  Khi các service Event/Ticket/Order/Payment/User (xem README gốc ở repo)
  sẵn sàng, cần thay `src/data/events.ts`, `OrganizerEventContext` và các
  Context còn lại bằng lệnh gọi API thực.
- **Đăng nhập/Đăng ký** chưa có trong bộ thiết kế gốc — được thêm mới theo
  yêu cầu, dùng chung design token (màu, bo góc, font) với các trang còn lại.
  Vai trò (Buyer/Organizer) chọn lúc đăng ký/đăng nhập được lưu vào một
  "directory" mock trong `localStorage` (`veluawa_user_directory`), đứng
  thay cho Keycloak.
- Design tokens (màu, font `Be Vietnam Pro`, bo góc, khoảng cách) được khai
  báo tại `src/index.css` (`@theme`) — sửa ở một chỗ, áp dụng toàn bộ site
  qua class Tailwind (`bg-gold`, `text-green`, `rounded-card`...).
