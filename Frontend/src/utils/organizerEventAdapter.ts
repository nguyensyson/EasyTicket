import type { CategoryKey, EventItem } from "@/types/event";
import type { EventDto, TicketTypeDto } from "@/types/eventApi";
import { formatDateLabel } from "@/utils/format";

/** Sự kiện PUBLISHED từ Event Service + loại vé của nó → EventItem dùng chung cho trang chủ/chi tiết/checkout.
 * `categoryKeyById` (từ `buildCategoryKeyLookup`, dựng từ `/api/v1/categories`) ánh xạ categoryId thật
 * sang 1 trong 4 tab danh mục ở trang chủ; thiếu thì mặc định "concert". `featured` do bên gọi xác định
 * (event có nằm trong nhóm bản ghi "Nổi bật" mà Event Service trả về hay không). */
export function eventDtoToItem(
  event: EventDto,
  ticketTypes: TicketTypeDto[],
  cityName?: string,
  categoryKeyById?: Map<string, CategoryKey>,
  featured = false,
): EventItem {
  return {
    id: event.id,
    title: event.title,
    category: categoryKeyById?.get(event.categoryId) ?? "concert",
    featured,
    date: event.startTime,
    dateLabel: formatDateLabel(event.startTime),
    venue: event.location,
    city: cityName ?? event.location,
    organizer: event.organizerId,
    description: event.description ?? "",
    tickets: ticketTypes.map((t) => ({ name: t.name, price: t.price })),
    organizerId: event.organizerId,
    categoryId: event.categoryId,
    categoryLabel: event.category,
    bannerUrl: event.bannerUrl ?? undefined,
  };
}
