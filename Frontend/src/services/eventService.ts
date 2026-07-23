import { apiRequest } from "@/lib/apiClient";
import type {
  CreateEventPayload,
  CreateFlashSalePayload,
  EventCategoryCode,
  EventDto,
  EventSearchParams,
  FlashSaleDto,
  LocationDto,
  OrganizerHistoryDto,
  PageResponse,
  TicketTypeDto,
  TicketTypePayload,
  UpdateEventPayload,
} from "@/types/eventApi";

// --- Locations & categories ---

export function listLocations(): Promise<LocationDto[]> {
  return apiRequest<LocationDto[]>("/api/v1/locations");
}

export function listCategories(): Promise<EventCategoryCode[]> {
  return apiRequest<EventCategoryCode[]>("/api/v1/events/categories");
}

// --- Public (buyer-facing) event browsing ---

export function searchPublishedEvents(
  params: EventSearchParams = {},
): Promise<PageResponse<EventDto>> {
  const qs = new URLSearchParams();
  if (params.category) qs.set("category", params.category);
  if (params.locationId) qs.set("locationId", params.locationId);
  if (params.from) qs.set("from", params.from);
  if (params.to) qs.set("to", params.to);
  qs.set("page", String(params.page ?? 0));
  qs.set("size", String(params.size ?? 50));
  return apiRequest<PageResponse<EventDto>>(`/api/v1/events?${qs.toString()}`);
}

export function getPublishedEvent(eventId: string): Promise<EventDto> {
  return apiRequest<EventDto>(`/api/v1/events/${eventId}`);
}

// --- Organizer: event management (Luồng 3) ---

export function listMyEvents(): Promise<EventDto[]> {
  return apiRequest<EventDto[]>("/api/v1/events/mine");
}

export function getManagedEvent(eventId: string): Promise<EventDto> {
  return apiRequest<EventDto>(`/api/v1/events/${eventId}/manage`);
}

export function createEvent(payload: CreateEventPayload): Promise<EventDto> {
  return apiRequest<EventDto>("/api/v1/events", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateEvent(
  eventId: string,
  payload: UpdateEventPayload,
): Promise<EventDto> {
  return apiRequest<EventDto>(`/api/v1/events/${eventId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function deleteEvent(eventId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/events/${eventId}`, { method: "DELETE" });
}

export function getOrganizerHistory(): Promise<OrganizerHistoryDto> {
  return apiRequest<OrganizerHistoryDto>("/api/v1/events/organizer-history");
}

// --- Ticket types (chỉ có thể tạo/sửa khi event còn DRAFT) ---

export function listTicketTypes(eventId: string): Promise<TicketTypeDto[]> {
  return apiRequest<TicketTypeDto[]>(`/api/v1/events/${eventId}/ticket-types`);
}

export function createTicketType(
  eventId: string,
  payload: TicketTypePayload,
): Promise<TicketTypeDto> {
  return apiRequest<TicketTypeDto>(`/api/v1/events/${eventId}/ticket-types`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateTicketType(
  eventId: string,
  ticketTypeId: string,
  payload: TicketTypePayload,
): Promise<TicketTypeDto> {
  return apiRequest<TicketTypeDto>(
    `/api/v1/events/${eventId}/ticket-types/${ticketTypeId}`,
    { method: "PUT", body: JSON.stringify(payload) },
  );
}

// --- Flash sale (chỉ tạo được 1 lần, khi event còn DRAFT — không có sửa/xoá) ---

export function getFlashSale(eventId: string): Promise<FlashSaleDto | null> {
  return apiRequest<FlashSaleDto | null>(`/api/v1/events/${eventId}/flash-sale`);
}

export function createFlashSale(
  eventId: string,
  payload: CreateFlashSalePayload,
): Promise<FlashSaleDto> {
  return apiRequest<FlashSaleDto>(`/api/v1/events/${eventId}/flash-sale`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
