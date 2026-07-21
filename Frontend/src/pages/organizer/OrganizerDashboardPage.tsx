import { useMemo } from "react";
import { Link } from "react-router-dom";
import { CalendarDays, Plus, Ticket, TrendingUp, Wallet } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { useOrganizerEvents } from "@/hooks/useOrganizerEvents";
import { useOrders } from "@/hooks/useOrders";
import { computeEventSales } from "@/utils/organizerStats";
import { formatPrice } from "@/utils/format";
import { StatCard } from "@/components/organizer/StatCard";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { Button } from "@/components/ui/Button";

export function OrganizerDashboardPage() {
  const { user } = useAuth();
  const { getMine } = useOrganizerEvents();
  const { orders } = useOrders();

  const myEvents = useMemo(
    () => (user ? getMine(user.email) : []),
    [user, getMine],
  );

  const summary = useMemo(() => {
    let totalRevenue = 0;
    let totalSold = 0;
    for (const event of myEvents) {
      const sales = computeEventSales(event, orders);
      totalRevenue += sales.totalRevenue;
      totalSold += sales.totalSold;
    }
    return {
      totalEvents: myEvents.length,
      published: myEvents.filter((e) => e.status === "PUBLISHED").length,
      totalRevenue,
      totalSold,
    };
  }, [myEvents, orders]);

  const recentEvents = [...myEvents]
    .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    .slice(0, 5);

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold">Tổng quan</h1>
          <p className="text-sm text-muted">Xin chào, {user?.name}</p>
        </div>
        <Link to="/organizer/events/new">
          <Button variant="green">
            <Plus className="h-4 w-4" /> Tạo sự kiện
          </Button>
        </Link>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          icon={CalendarDays}
          label="Tổng số sự kiện"
          value={String(summary.totalEvents)}
        />
        <StatCard
          icon={Ticket}
          label="Đã xuất bản"
          value={String(summary.published)}
          hint={`${summary.totalEvents - summary.published} nháp/đã hủy`}
        />
        <StatCard
          icon={TrendingUp}
          label="Tổng vé đã bán"
          value={String(summary.totalSold)}
        />
        <StatCard
          icon={Wallet}
          label="Tổng doanh thu"
          value={formatPrice(summary.totalRevenue)}
        />
      </div>

      <div className="rounded-card border border-border bg-white">
        <div className="flex items-center justify-between border-b border-border px-5 py-4">
          <h2 className="text-base font-bold">Sự kiện gần đây</h2>
          <Link
            to="/organizer/events"
            className="text-sm font-semibold text-green"
          >
            Xem tất cả
          </Link>
        </div>

        {recentEvents.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-14 text-center">
            <p className="text-[15px] text-[#8a8a80]">
              Bạn chưa tạo sự kiện nào.
            </p>
            <Link to="/organizer/events/new">
              <Button variant="green">
                <Plus className="h-4 w-4" /> Tạo sự kiện đầu tiên
              </Button>
            </Link>
          </div>
        ) : (
          <div className="divide-y divide-border-soft">
            {recentEvents.map((event) => (
              <Link
                key={event.id}
                to={`/organizer/events/${event.id}/dashboard`}
                className="flex items-center justify-between gap-3 px-5 py-3.5 hover:bg-cream"
              >
                <div className="min-w-0">
                  <div className="truncate text-sm font-bold">
                    {event.title}
                  </div>
                  <div className="text-xs text-muted">
                    {event.locationName} ·{" "}
                    {new Date(event.startTime).toLocaleDateString("vi-VN")}
                  </div>
                </div>
                <StatusBadge status={event.status} />
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
