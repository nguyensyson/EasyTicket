import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { formatPrice } from "@/utils/format";
import { ApiError } from "@/lib/apiClient";
import { getTicketHistory } from "@/services/userService";
import { getPublishedEvent, listTicketTypes } from "@/services/eventService";
import type { OrderStatus, TicketHistoryItemDto } from "@/types/orderApi";

const STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING_PAYMENT: "Chờ thanh toán",
  PAID: "Đã thanh toán",
  CANCELLED: "Đã hủy",
};

const STATUS_CLASS: Record<OrderStatus, string> = {
  PENDING_PAYMENT: "bg-[#F5E6C8] text-[#8a6d1f]",
  PAID: "bg-green-tint text-green",
  CANCELLED: "bg-[#F4D9D4] text-[#A23B2E]",
};

function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span
      className={`inline-block rounded-pill px-2.5 py-1 text-xs font-bold ${STATUS_CLASS[status]}`}
    >
      {STATUS_LABEL[status]}
    </span>
  );
}

interface EventInfo {
  title: string;
  ticketTypeNameById: Map<string, string>;
}

export function OrderHistoryPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<TicketHistoryItemDto[]>([]);
  const [eventInfoById, setEventInfoById] = useState<Map<string, EventInfo>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;
    let cancelled = false;
    setLoading(true);
    setError(null);

    getTicketHistory()
      .then(async (items) => {
        if (cancelled) return;
        setOrders(items);

        const eventIds = [...new Set(items.map((o) => o.eventId))];
        const infoEntries = await Promise.all(
          eventIds.map(async (eventId): Promise<[string, EventInfo]> => {
            const [event, ticketTypes] = await Promise.all([
              getPublishedEvent(eventId).catch(() => null),
              listTicketTypes(eventId).catch(() => []),
            ]);
            return [
              eventId,
              {
                title: event?.title ?? eventId,
                ticketTypeNameById: new Map(ticketTypes.map((t) => [t.id, t.name])),
              },
            ];
          }),
        );
        if (!cancelled) setEventInfoById(new Map(infoEntries));
      })
      .catch((err) => {
        if (!cancelled) {
          setError(
            err instanceof ApiError ? err.message : "Không thể tải lịch sử vé.",
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [user]);

  if (!user) {
    return <Navigate to="/login" state={{ from: "/account/orders" }} replace />;
  }

  return (
    <main className="mx-auto w-full max-w-[840px] flex-1 px-4 py-8 sm:px-10">
      <h1 className="mb-1 text-2xl font-extrabold">Đơn hàng của tôi</h1>
      <p className="mb-6 text-sm text-muted">Xin chào, {user.name}</p>

      {error && (
        <div className="mb-5 rounded-card border border-[#E0A9A0] bg-[#F4D9D4] p-4 text-sm text-[#A23B2E]">
          {error}
        </div>
      )}

      {loading ? (
        <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
          Đang tải...
        </div>
      ) : orders.length === 0 ? (
        <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
          Chưa có đơn hàng nào.
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {orders.map((order) => {
            const info = eventInfoById.get(order.eventId);
            const ticketTypeName = info?.ticketTypeNameById.get(order.ticketTypeId) ?? "Vé";
            return (
              <div
                key={order.id}
                className="rounded-card border border-border bg-white p-5.5"
              >
                <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <div className="text-[15px] font-bold">
                      {info?.title ?? order.eventId}
                    </div>
                    <div className="text-xs text-muted">
                      Mã đơn: <span className="font-semibold text-gold">{order.id}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2.5">
                    <span className="text-xs text-muted">
                      {new Date(order.createdAt).toLocaleString("vi-VN")}
                    </span>
                    <OrderStatusBadge status={order.status} />
                  </div>
                </div>
                <div className="border-t border-border-soft pt-3">
                  <div className="flex justify-between py-1 text-sm text-[#3a3a3a]">
                    <span>
                      {ticketTypeName} × {order.quantity}
                    </span>
                    <span>{formatPrice(order.unitPrice * order.quantity)}</span>
                  </div>
                  <div className="mt-2 flex justify-between border-t border-border-soft pt-2 text-sm font-bold">
                    <span>Tổng</span>
                    <span className="text-gold">{formatPrice(order.totalAmount)}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </main>
  );
}
