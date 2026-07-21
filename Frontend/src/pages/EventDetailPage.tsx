import { useMemo, useRef } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { Calendar, MapPin } from "lucide-react";
import { useEventLookup } from "@/hooks/useAllEvents";
import { getEventImageStyle, getCategoryLabel } from "@/utils/eventImage";
import { formatMinPrice, formatPrice } from "@/utils/format";
import { TicketRow } from "@/components/events/TicketRow";
import { Button } from "@/components/ui/Button";
import { useCart } from "@/hooks/useCart";

export function EventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const event = useEventLookup(id);
  const { getEventCart, incQty, decQty } = useCart();
  const ticketPanelRef = useRef<HTMLDivElement>(null);

  const eventCart = event ? getEventCart(event.id) : {};

  const totalCount = useMemo(
    () => Object.values(eventCart).reduce((s, q) => s + q, 0),
    [eventCart],
  );
  const totalPrice = useMemo(() => {
    if (!event) return 0;
    return event.tickets.reduce(
      (s, t) => s + t.price * (eventCart[t.name] || 0),
      0,
    );
  }, [event, eventCart]);

  if (!event) {
    return <Navigate to="/" replace />;
  }

  function scrollToTickets() {
    if (ticketPanelRef.current) {
      const top =
        ticketPanelRef.current.getBoundingClientRect().top +
        window.scrollY -
        90;
      window.scrollTo({ top, behavior: "smooth" });
    }
  }

  return (
    <main className="flex-1">
      <div className="bg-dark px-4 pb-15 pt-8 sm:px-10">
        <Link
          to="/"
          className="mb-5.5 inline-block text-sm font-semibold text-gold"
        >
          ← Tất cả sự kiện
        </Link>
        <div className="mx-auto flex max-w-[1160px] flex-wrap items-stretch gap-6">
          <div className="flex min-w-[300px] max-w-full flex-1 flex-col rounded-lg bg-cream p-6.5 lg:max-w-[380px]">
            <span className="mb-3.5 inline-block self-start rounded-pill bg-gold px-3 py-1.5 text-xs font-bold text-dark">
              {getCategoryLabel(event.category)}
            </span>
            <h1 className="mb-4.5 text-2xl font-extrabold leading-snug text-ink">
              {event.title}
            </h1>
            <div className="mb-3.5 flex items-start gap-2.5">
              <Calendar className="mt-0.5 h-4.5 w-4.5 shrink-0 text-green" />
              <div className="text-sm font-semibold text-green">
                {event.dateLabel}
              </div>
            </div>
            <div className="mb-5 flex items-start gap-2.5">
              <MapPin className="mt-0.5 h-4.5 w-4.5 shrink-0 text-green" />
              <div>
                <div className="text-sm font-bold text-green">{event.venue}</div>
                <div className="text-[13px] text-muted-soft">{event.city}</div>
              </div>
            </div>
            <div className="my-1 border-t border-border-soft" />
            <div className="mb-1 mt-4.5 text-[13px] text-muted-soft">
              Giá từ
            </div>
            <div className="mb-5 text-[22px] font-extrabold text-green">
              {formatMinPrice(event.tickets.map((t) => t.price))}
            </div>
            <Button variant="green" className="mt-auto w-full" onClick={scrollToTickets}>
              Mua vé ngay
            </Button>
          </div>
          <div
            className="relative min-w-[280px] flex-[1.4] overflow-hidden rounded-lg"
            style={getEventImageStyle(event.category)}
          >
            <span className="absolute bottom-4 left-4 rounded-md bg-[rgba(22,22,22,0.7)] px-2.5 py-1.5 font-mono text-xs text-cream">
              event image
            </span>
          </div>
        </div>
      </div>

      <div className="mx-auto flex max-w-[1160px] flex-wrap items-start gap-6 px-4 py-9 sm:px-10">
        <div className="min-w-[300px] flex-[2] overflow-hidden rounded-card bg-dark">
          <div className="border-b border-dark-border px-5.5 py-4 text-[15px] font-bold text-gold">
            Giới thiệu
          </div>
          <div className="p-5.5">
            <p className="mb-4.5 text-[15px] leading-[1.8] text-[#D8D3C4]">
              {event.description}
            </p>
            <div className="flex flex-wrap gap-6 text-[13px] text-[#B8B8AE]">
              <div>
                <strong className="text-gold">Ban tổ chức</strong>
                <br />
                {event.organizer}
              </div>
            </div>
          </div>
        </div>

        <div ref={ticketPanelRef} className="min-w-[280px] flex-1 lg:sticky lg:top-24">
          <div className="rounded-card border border-border bg-white p-5.5">
            <h3 className="mb-4 text-base font-bold">Chọn loại vé</h3>
            {event.tickets.map((t) => (
              <TicketRow
                key={t.name}
                ticket={t}
                qty={eventCart[t.name] || 0}
                onInc={() => incQty(event.id, t.name)}
                onDec={() => decQty(event.id, t.name)}
              />
            ))}
            <div className="flex justify-between py-4 text-[15px] font-bold">
              <span>Tổng cộng</span>
              <span className="text-gold">{formatPrice(totalPrice)}</span>
            </div>
            <Button
              variant="gold"
              className="mt-1.5 w-full"
              disabled={totalCount === 0}
              onClick={() => navigate(`/checkout/${event.id}`)}
            >
              Tiếp tục thanh toán
            </Button>
          </div>
        </div>
      </div>
    </main>
  );
}
