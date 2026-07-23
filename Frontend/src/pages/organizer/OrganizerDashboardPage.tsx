import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { CalendarDays, Plus, Ticket, TrendingUp, Wallet } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { formatPrice } from "@/utils/format";
import { StatCard } from "@/components/organizer/StatCard";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { Button } from "@/components/ui/Button";
import { ApiError } from "@/lib/apiClient";
import { getOrganizerHistory } from "@/services/eventService";
import type { OrganizerHistoryDto } from "@/types/eventApi";

export function OrganizerDashboardPage() {
  const { user } = useAuth();
  const [history, setHistory] = useState<OrganizerHistoryDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getOrganizerHistory()
      .then((data) => {
        if (!cancelled) setHistory(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(
            err instanceof ApiError ? err.message : "Không thể tải dữ liệu tổng quan.",
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const published = history?.events.filter((e) => e.status === "PUBLISHED").length ?? 0;
  const totalEvents = history?.totalEvents ?? 0;
  const recentEvents = [...(history?.events ?? [])]
    .sort((a, b) => b.startTime.localeCompare(a.startTime))
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

      {error && (
        <div className="mb-5 rounded-card border border-[#E0A9A0] bg-[#F4D9D4] p-4 text-sm text-[#A23B2E]">
          {error}
        </div>
      )}

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          icon={CalendarDays}
          label="Tổng số sự kiện"
          value={loading ? "…" : String(totalEvents)}
        />
        <StatCard
          icon={Ticket}
          label="Đã xuất bản"
          value={loading ? "…" : String(published)}
          hint={loading ? undefined : `${totalEvents - published} nháp/đã hủy`}
        />
        <StatCard
          icon={TrendingUp}
          label="Tổng vé đã bán"
          value={loading ? "…" : String(history?.totalTicketsSold ?? 0)}
        />
        <StatCard
          icon={Wallet}
          label="Tổng doanh thu"
          value={loading ? "…" : formatPrice(history?.totalRevenue ?? 0)}
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

        {loading ? (
          <div className="py-14 text-center text-[15px] text-[#8a8a80]">Đang tải...</div>
        ) : recentEvents.length === 0 ? (
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
                key={event.eventId}
                to={`/organizer/events/${event.eventId}/dashboard`}
                className="flex items-center justify-between gap-3 px-5 py-3.5 hover:bg-cream"
              >
                <div className="min-w-0">
                  <div className="truncate text-sm font-bold">
                    {event.title}
                  </div>
                  <div className="text-xs text-muted">
                    {new Date(event.startTime).toLocaleDateString("vi-VN")} ·{" "}
                    {event.ticketsSold} vé đã bán
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
