export type CategoryKey = "concert" | "sankhau" | "thethao" | "hoithao";

export interface TicketType {
  name: string;
  price: number;
}

export interface EventItem {
  id: string;
  title: string;
  category: CategoryKey;
  featured: boolean;
  date: string;
  dateLabel: string;
  venue: string;
  city: string;
  organizer: string;
  description: string;
  tickets: TicketType[];
  /** Có mặt khi sự kiện đến từ Organizer (không phải seed data tĩnh) — dùng để phân biệt nguồn khi hiển thị. */
  organizerId?: string;
  /** categoryId thật (bảng categories) — chỉ có ở sự kiện lấy từ Event Service, dùng để lọc theo tab danh mục thật. */
  categoryId?: string;
  /** Tên danh mục thật (CategoryDto.name, vd. "Triển lãm & Hội chợ") — ưu tiên hiển thị thay cho label nhóm cũ. */
  categoryLabel?: string;
  /** Ảnh banner thật của sự kiện (EventDto.bannerUrl) — sự kiện demo tĩnh không có, dùng placeholder. */
  bannerUrl?: string;
}

// --- Nghiệp vụ Organizer — bám theo schema events/ticket_types/flash_sales ở README (Event Service) ---

export type UserRole = "buyer" | "organizer";

export type EventStatus = "DRAFT" | "PUBLISHED" | "CANCELLED";

export interface TicketTypeDef {
  id: string;
  name: string;
  price: number;
  totalQuantity: number;
}

export interface FlashSaleDef {
  startAt: string; // ISO datetime-local
  endAt: string;
}

export interface OrganizerEvent {
  id: string;
  organizerId: string; // = email người tạo (mock, thay cho Keycloak user UUID)
  organizerName: string;
  title: string;
  description: string;
  category: CategoryKey;
  locationName: string; // thành phố/tỉnh — tương ứng locations.name / location_id
  address: string; // địa chỉ cụ thể — tương ứng cột `location`
  startTime: string; // ISO datetime-local
  endTime: string;
  status: EventStatus;
  ticketTypes: TicketTypeDef[];
  flashSale: FlashSaleDef | null;
  deleteFlag: "ACTIVE" | "DELETED";
  createdAt: string;
  updatedAt: string;
}

export interface CategoryStyle {
  bg: string;
  stripe: string;
  label: string;
}

export interface CartLine {
  name: string;
  qty: number;
  price: number;
}

export interface Order {
  id: string;
  eventId: string;
  eventTitle: string;
  lines: CartLine[];
  total: number;
  buyerName: string;
  buyerEmail: string;
  buyerPhone: string;
  payment: string;
  createdAt: string;
}
