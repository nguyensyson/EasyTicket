import { useEffect } from "react";
import { Check } from "lucide-react";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { useOrders } from "@/hooks/useOrders";
import { useCart } from "@/hooks/useCart";
import { Button } from "@/components/ui/Button";

export function OrderSuccessPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const { getOrder } = useOrders();
  const { clearEventCart } = useCart();
  const navigate = useNavigate();
  const order = getOrder(orderId || "");

  useEffect(() => {
    if (order) clearEventCart(order.eventId);
  }, [order, clearEventCart]);

  if (!order) {
    return <Navigate to="/" replace />;
  }

  return (
    <main className="flex flex-1 items-center justify-center px-4 py-15 sm:px-10">
      <div className="max-w-[440px] text-center">
        <div className="mx-auto mb-6 flex h-18 w-18 items-center justify-center rounded-full bg-green">
          <Check className="h-8 w-8 text-cream" strokeWidth={3} />
        </div>
        <h1 className="mb-3 text-2xl font-extrabold">Đặt vé thành công!</h1>
        <p className="mb-1.5 text-sm text-muted">Mã đơn hàng của bạn</p>
        <p className="mb-6 text-xl font-extrabold tracking-wide text-gold">
          {order.id}
        </p>
        <p className="mb-7 text-sm text-[#3a3a3a]">
          Vé điện tử đã được gửi tới {order.buyerEmail}.
        </p>
        <Button variant="dark" onClick={() => navigate("/")}>
          Về trang chủ
        </Button>
      </div>
    </main>
  );
}
