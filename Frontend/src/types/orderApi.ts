// --- Kiểu dữ liệu khớp DTO thật của Order Service, trả về qua UserService aggregation (Luồng 8) ---
// Xem README mục "Thiết kế Database" > Order Service (order_db.orders).

export type OrderStatus = "PENDING_PAYMENT" | "PAID" | "CANCELLED";

export interface TicketHistoryItemDto {
  id: string;
  eventId: string;
  ticketTypeId: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
}
