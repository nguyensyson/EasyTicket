import { useEffect, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { Pencil, TrendingUp, Wallet, Zap } from "lucide-react";
import { getFlashSaleStatus } from "@/utils/organizerStats";
import { formatDateTimeLabel, formatPrice } from "@/utils/format";
import { StatCard } from "@/components/organizer/StatCard";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { Button } from "@/components/ui/Button";
import { ApiError } from "@/lib/apiClient";
import {
  getFlashSale,
  getManagedEvent,
  getOrganizerHistory,
  listTicketTypes,
} from "@/services/eventService";
import type { EventDto, FlashSaleDto, TicketTypeDto } from "@/types/eventApi";

const FLASH_STATUS_LABEL: Record<string, string> = {
  SCHEDULED: "Chưa bắt đầu",
  ACTIVE: "Đang diễn ra",
  ENDED: "Đã kết thúc",
};

export function OrganizerEventDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const [event, setEvent] = useState<EventDto | null>(null);
  const [ticketTypes, setTicketTypes] = useState<TicketTypeDto[]>([]);
  const [flashSale, setFlashSale] = useState<FlashSaleDto | null>(null);
  const [stats, setStats] = useState<{ ticketsSold: number; revenue: number } | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    setLoading(true);
    setNotFound(false);

    Promise.all([
      getManagedEvent(id),
      listTicketTypes(id).catch(() => []),
      getFlashSale(id).catch(() => null),
      getOrganizerHistory().catch(() => null),
    ])
      .then(([eventDto, types, flash, history]) => {
        if (cancelled) return;
        setEvent(eventDto);
        setTicketTypes(types);
        setFlashSale(flash);
        const stat = history?.events.find((e) => e.eventId === id);
        setStats({ ticketsSold: stat?.ticketsSold ?? 0, revenue: stat?.revenue ?? 0 });
      })
      .catch((err) => {
        if (!cancelled && err instanceof ApiError) setNotFound(true);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (!id || notFound) {
    return <Navigate to="/organizer/events" replace />;
  }

  if (loading || !event) {
    return (
      <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
        Đang tải...
      </div>
    );
  }

  const flashStatus = getFlashSaleStatus(flashSale);
  const totalCapacity = ticketTypes.reduce((s, t) => s + t.totalQuantity, 0);

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="mb-1.5 flex flex-wrap items-center gap-2">
            <StatusBadge status={event.status} />
            <span className="rounded-pill bg-[#EDE6D9] px-2.5 py-1 text-xs font-semibold text-[#5a5a52]">
              {event.category}
            </span>
          </div>
          <h1 className="text-2xl font-extrabold">{event.title}</h1>
          <p className="text-sm text-muted">{event.location}</p>
        </div>
        <div className="flex gap-2.5">
          {event.status === "PUBLISHED" && (
            <Link to={`/events/${event.id}`} target="_blank">
              <Button variant="outline-gold" size="sm">
                Xem trang công khai
              </Button>
            </Link>
          )}
          <Link to={`/organizer/events/${event.id}/edit`}>
            <Button variant="dark" size="sm">
              <Pencil className="h-3.5 w-3.5" /> Sửa sự kiện
            </Button>
          </Link>
        </div>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard
          icon={Wallet}
          label="Doanh thu"
          value={formatPrice(stats?.revenue ?? 0)}
        />
        <StatCard
          icon={TrendingUp}
          label="Vé đã bán"
          value={`${stats?.ticketsSold ?? 0} / ${totalCapacity}`}
          hint={
            totalCapacity > 0
              ? `${Math.round(((stats?.ticketsSold ?? 0) / totalCapacity) * 100)}% tổng vé phát hành`
              : undefined
          }
        />
        <StatCard
          icon={Zap}
          label="Flash Sale"
          value={flashStatus ? FLASH_STATUS_LABEL[flashStatus] : "Chưa cấu hình"}
          hint={
            flashSale
              ? `${formatDateTimeLabel(flashSale.startAt)} → ${formatDateTimeLabel(flashSale.endAt)}`
              : undefined
          }
        />
      </div>

      <div className="rounded-card border border-border bg-white">
        <div className="border-b border-border px-5 py-4">
          <h2 className="text-base font-bold">Loại vé</h2>
        </div>
        {ticketTypes.length === 0 ? (
          <div className="py-14 text-center text-[15px] text-[#8a8a80]">
            Sự kiện chưa có loại vé nào.
          </div>
        ) : (
          <div className="divide-y divide-border-soft">
            {ticketTypes.map((t) => (
              <div key={t.id} className="flex items-center justify-between px-5 py-4">
                <div>
                  <div className="text-sm font-bold">{t.name}</div>
                  <div className="text-xs text-muted">{formatPrice(t.price)} / vé</div>
                </div>
                <div className="text-sm font-bold text-gold">
                  {t.totalQuantity} vé phát hành
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
