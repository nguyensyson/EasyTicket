import type { EventItem, OrganizerEvent } from "@/types/event";
import { formatDateLabel } from "@/utils/format";

export function organizerEventToItem(oe: OrganizerEvent): EventItem {
  return {
    id: oe.id,
    title: oe.title,
    category: oe.category,
    featured: false,
    date: oe.startTime,
    dateLabel: formatDateLabel(oe.startTime),
    venue: oe.address,
    city: oe.locationName,
    organizer: oe.organizerName,
    description: oe.description,
    tickets: oe.ticketTypes.map((t) => ({ name: t.name, price: t.price })),
    organizerId: oe.organizerId,
  };
}
