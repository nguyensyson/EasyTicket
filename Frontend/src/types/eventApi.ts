import type { EventStatus } from "@/types/event";

// --- Kiểu dữ liệu khớp DTO thật của Event Service (xem README mục "API Endpoints" / "Thiết kế Database") ---

export type EventCategoryCode =
  | "MUSIC"
  | "SPORTS"
  | "WORKSHOP"
  | "THEATER"
  | "CONFERENCE"
  | "OTHER";

export interface LocationDto {
  id: string;
  name: string;
}

export interface CategoryDto {
  id: string;
  name: string;
}

export interface EventDto {
  id: string;
  organizerId: string;
  title: string;
  description: string | null;
  categoryId: string;
  category: string; // tên danh mục (join sẵn từ Event Service), không phải EventCategoryCode
  locationId: string;
  location: string;
  bannerUrl: string | null;
  startTime: string; // ISO datetime, LocalDateTime không hậu tố "Z"
  endTime: string;
  status: EventStatus;
}

export interface TicketTypeDto {
  id: string;
  eventId: string;
  name: string;
  price: number;
  totalQuantity: number;
}

export type FlashSaleStatusCode = "SCHEDULED" | "ACTIVE" | "ENDED";

export interface FlashSaleDto {
  id: string;
  eventId: string;
  startAt: string;
  endAt: string;
  status: FlashSaleStatusCode;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateEventPayload {
  title: string;
  description: string;
  category: EventCategoryCode;
  locationId: string;
  location: string;
  bannerUrl?: string;
  startTime: string;
  endTime: string;
}

export interface UpdateEventPayload extends CreateEventPayload {
  status: EventStatus;
}

export interface TicketTypePayload {
  name: string;
  price: number;
  totalQuantity: number;
}

export interface CreateFlashSalePayload {
  startAt: string;
  endAt: string;
}

export interface EventSearchParams {
  categoryId?: string;
  locationId?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface OrganizerEventStatsDto {
  eventId: string;
  title: string;
  status: EventStatus;
  startTime: string;
  endTime: string;
  ticketsSold: number;
  revenue: number;
}

export interface OrganizerHistoryDto {
  totalEvents: number;
  totalTicketsSold: number;
  totalRevenue: number;
  events: OrganizerEventStatsDto[];
}
