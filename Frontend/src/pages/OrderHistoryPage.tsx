import { Navigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useOrders } from "@/hooks/useOrders";
import { formatPrice } from "@/utils/format";

export function OrderHistoryPage() {
  const { user } = useAuth();
  const { orders } = useOrders();

  if (!user) {
    return <Navigate to="/login" state={{ from: "/account/orders" }} replace />;
  }

  const myOrders = orders.filter((o) => o.buyerEmail === user.email);

  return (
    <main className="mx-auto w-full max-w-[840px] flex-1 px-4 py-8 sm:px-10">
      <h1 className="mb-1 text-2xl font-extrabold">Đơn hàng của tôi</h1>
      <p className="mb-6 text-sm text-muted">Xin chào, {user.name}</p>

      {myOrders.length === 0 ? (
        <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
          Chưa có đơn hàng nào.
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {myOrders.map((order) => (
            <div
              key={order.id}
              className="rounded-card border border-border bg-white p-5.5"
            >
              <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                <div>
                  <div className="text-[15px] font-bold">{order.eventTitle}</div>
                  <div className="text-xs text-muted">
                    Mã đơn: <span className="font-semibold text-gold">{order.id}</span>
                  </div>
                </div>
                <div className="text-xs text-muted">
                  {new Date(order.createdAt).toLocaleString("vi-VN")}
                </div>
              </div>
              <div className="border-t border-border-soft pt-3">
                {order.lines.map((line) => (
                  <div
                    key={line.name}
                    className="flex justify-between py-1 text-sm text-[#3a3a3a]"
                  >
                    <span>
                      {line.name} × {line.qty}
                    </span>
                    <span>{formatPrice(line.price * line.qty)}</span>
                  </div>
                ))}
                <div className="mt-2 flex justify-between border-t border-border-soft pt-2 text-sm font-bold">
                  <span>Tổng</span>
                  <span className="text-gold">{formatPrice(order.total)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
