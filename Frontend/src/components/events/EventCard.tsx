import { Link } from "react-router-dom";
import type { EventItem } from "@/types/event";
import { getEventCategoryDisplayLabel, getEventImageStyle } from "@/utils/eventImage";
import { formatMinPrice } from "@/utils/format";

export function EventCard({ event }: { event: EventItem }) {
  return (
    <Link
      to={`/events/${event.id}`}
      className="group overflow-hidden rounded-card border border-border bg-white transition-all duration-150 hover:-translate-y-1 hover:shadow-[0_12px_28px_rgba(22,22,22,0.12)]"
    >
      <div
        className="relative h-40"
        style={event.bannerUrl ? undefined : getEventImageStyle(event.category)}
      >
        {event.bannerUrl ? (
          <img
            src={event.bannerUrl}
            alt={event.title}
            className="h-full w-full object-cover"
          />
        ) : (
          <span className="absolute bottom-2.5 left-2.5 rounded-md bg-[rgba(22,22,22,0.65)] px-2 py-1 font-mono text-[11px] text-cream">
            event image
          </span>
        )}
        <span className="absolute right-2.5 top-2.5 rounded-pill bg-gold px-2.5 py-1 text-[11px] font-bold text-dark">
          {getEventCategoryDisplayLabel(event)}
        </span>
      </div>
      <div className="p-4">
        <div className="mb-1.5 text-xs font-bold text-green">
          {event.dateLabel}
        </div>
        <div className="mb-1.5 line-clamp-2 text-base font-bold leading-tight text-ink">
          {event.title}
        </div>
        <div className="mb-2.5 text-[13px] text-muted">
          {event.venue} · {event.city}
        </div>
        <div className="text-[15px] font-extrabold text-gold">
          Từ {formatMinPrice(event.tickets.map((t) => t.price))}
        </div>
      </div>
    </Link>
  );
}
