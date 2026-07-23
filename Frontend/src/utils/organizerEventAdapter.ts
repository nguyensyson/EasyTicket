import type { EventItem } from "@/types/event";
import type { EventDto, TicketTypeDto } from "@/types/eventApi";
import { formatDateLabel } from "@/utils/format";
import { toCategoryKey } from "@/utils/eventCategory";

/** Sự kiện PUBLISHED từ Event Service + loại vé của nó → EventItem dùng chung cho trang chủ/chi tiết/checkout. */
export function eventDtoToItem(
  event: EventDto,
  ticketTypes: TicketTypeDto[],
  cityName?: string,
): EventItem {
  return {
    id: event.id,
    title: event.title,
    category: toCategoryKey(event.category),
    featured: false,
    date: event.startTime,
    dateLabel: formatDateLabel(event.startTime),
    venue: event.location,
    city: cityName ?? event.location,
    organizer: event.organizerId,
    description: event.description ?? "",
    tickets: ticketTypes.map((t) => ({ name: t.name, price: t.price })),
    organizerId: event.organizerId,
  };
}
