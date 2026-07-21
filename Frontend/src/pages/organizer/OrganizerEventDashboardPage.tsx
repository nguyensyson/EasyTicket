import { useMemo } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { Pencil, TrendingUp, Wallet, Zap } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { useOrganizerEvents } from "@/hooks/useOrganizerEvents";
import { useOrders } from "@/hooks/useOrders";
import { computeEventSales, getFlashSaleStatus } from "@/utils/organizerStats";
import { formatDateTimeLabel, formatPrice } from "@/utils/format";
import { getCategoryLabel } from "@/utils/eventImage";
import { StatCard } from "@/components/organizer/StatCard";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { Button } from "@/components/ui/Button";

const FLASH_STATUS_LABEL: Record<string, string> = {
  SCHEDULED: "Chưa bắt đầu",
  ACTIVE: "Đang diễn ra",
  ENDED: "Đã kết thúc",
};

export function OrganizerEventDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const { getById } = useOrganizerEvents();
  const { orders } = useOrders();

  const event = id ? getById(id) : undefined;
  const sales = useMemo(
    () => (event ? computeEventSales(event, orders) : null),
    [event, orders],
  );

  if (!event || !user || event.organizerId !== user.email) {
    return <Navigate to="/organizer/events" replace />;
  }

  const flashStatus = getFlashSaleStatus(event.flashSale);

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="mb-1.5 flex flex-wrap items-center gap-2">
            <StatusBadge status={event.status} />
            <span className="rounded-pill bg-[#EDE6D9] px-2.5 py-1 text-xs font-semibold text-[#5a5a52]">
              {getCategoryLabel(event.category)}
            </span>
          </div>
          <h1 className="text-2xl font-extrabold">{event.title}</h1>
          <p className="text-sm text-muted">
            {event.locationName} · {event.address}
          </p>
        </div>
        <div className="flex gap-2.5">
          <Link to={`/events/${event.id}`} target="_blank">
            <Button variant="outline-gold" size="sm">
              Xem trang công khai
            </Button>
          </Link>
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
          value={formatPrice(sales?.totalRevenue ?? 0)}
        />
        <StatCard
          icon={TrendingUp}
          label="Vé đã bán"
          value={`${sales?.totalSold ?? 0} / ${sales?.totalCapacity ?? 0}`}
          hint={
            sales && sales.totalCapacity > 0
              ? `${Math.round((sales.totalSold / sales.totalCapacity) * 100)}% tổng vé phát hành`
              : undefined
          }
        />
        <StatCard
          icon={Zap}
          label="Flash Sale"
          value={flashStatus ? FLASH_STATUS_LABEL[flashStatus] : "Chưa cấu hình"}
          hint={
            event.flashSale
              ? `${formatDateTimeLabel(event.flashSale.startAt)} → ${formatDateTimeLabel(event.flashSale.endAt)}`
              : undefined
          }
        />
      </div>

      <div className="rounded-card border border-border bg-white">
        <div className="border-b border-border px-5 py-4">
          <h2 className="text-base font-bold">Doanh thu theo loại vé</h2>
        </div>
        {!sales || sales.rows.length === 0 ? (
          <div className="py-14 text-center text-[15px] text-[#8a8a80]">
            Sự kiện chưa có loại vé nào.
          </div>
        ) : (
          <div className="divide-y divide-border-soft">
            {sales.rows.map((row) => {
              const pct =
                row.totalQuantity > 0
                  ? Math.min(100, Math.round((row.sold / row.totalQuantity) * 100))
                  : 0;
              return (
                <div key={row.name} className="px-5 py-4">
                  <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <div className="text-sm font-bold">{row.name}</div>
                      <div className="text-xs text-muted">
                        {formatPrice(row.price)} / vé
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-bold text-gold">
                        {formatPrice(row.revenue)}
                      </div>
                      <div className="text-xs text-muted">
                        {row.sold}/{row.totalQuantity} vé
                      </div>
                    </div>
                  </div>
                  <div className="h-2 w-full overflow-hidden rounded-pill bg-cream">
                    <div
                      className="h-full rounded-pill bg-green"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
