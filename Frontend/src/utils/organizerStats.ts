import type { Order, OrganizerEvent } from "@/types/event";

export interface TicketSalesRow {
  name: string;
  price: number;
  totalQuantity: number;
  sold: number;
  revenue: number;
}

export interface EventSalesSummary {
  totalRevenue: number;
  totalSold: number;
  totalCapacity: number;
  rows: TicketSalesRow[];
}

/**
 * Doanh thu/vé bán được tổng hợp trực tiếp từ các Order thật đã tạo qua luồng
 * checkout (OrderContext) — không dùng số liệu giả lập, giống nguyên tắc
 * "GET dashboard tổng hợp theo lô từ Order Service" mô tả trong README backend.
 */
export function computeEventSales(
  event: OrganizerEvent,
  orders: Order[],
): EventSalesSummary {
  const eventOrders = orders.filter((o) => o.eventId === event.id);

  const rows: TicketSalesRow[] = event.ticketTypes.map((t) => {
    let sold = 0;
    let revenue = 0;
    for (const order of eventOrders) {
      for (const line of order.lines) {
        if (line.name === t.name) {
          sold += line.qty;
          revenue += line.qty * line.price;
        }
      }
    }
    return {
      name: t.name,
      price: t.price,
      totalQuantity: t.totalQuantity,
      sold,
      revenue,
    };
  });

  return {
    totalRevenue: rows.reduce((s, r) => s + r.revenue, 0),
    totalSold: rows.reduce((s, r) => s + r.sold, 0),
    totalCapacity: rows.reduce((s, r) => s + r.totalQuantity, 0),
    rows,
  };
}

export type FlashSaleRuntimeStatus = "SCHEDULED" | "ACTIVE" | "ENDED";

export function getFlashSaleStatus(
  flashSale: { startAt: string; endAt: string } | null,
): FlashSaleRuntimeStatus | null {
  if (!flashSale) return null;
  const now = Date.now();
  const start = new Date(flashSale.startAt).getTime();
  const end = new Date(flashSale.endAt).getTime();
  if (now < start) return "SCHEDULED";
  if (now > end) return "ENDED";
  return "ACTIVE";
}
