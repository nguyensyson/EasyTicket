import { useMemo, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { useEventLookup } from "@/hooks/useAllEvents";
import { formatPrice, generateOrderId } from "@/utils/format";
import { PaymentOption } from "@/components/checkout/PaymentOption";
import { Button } from "@/components/ui/Button";
import { useCart } from "@/hooks/useCart";
import { useAuth } from "@/hooks/useAuth";
import { useOrders } from "@/hooks/useOrders";
import type { CartLine, Order } from "@/types/event";

const PAYMENT_OPTIONS = [
  { key: "evi", label: "Ví điện tử" },
  { key: "card", label: "Thẻ ngân hàng" },
  { key: "transfer", label: "Chuyển khoản" },
];

export function CheckoutPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const event = useEventLookup(id);
  const { getEventCart } = useCart();
  const { user } = useAuth();
  const { addOrder } = useOrders();

  const [buyerName, setBuyerName] = useState(user?.name || "");
  const [buyerEmail, setBuyerEmail] = useState(user?.email || "");
  const [buyerPhone, setBuyerPhone] = useState("");
  const [payment, setPayment] = useState("evi");

  const eventCart = event ? getEventCart(event.id) : {};

  const cartLines: CartLine[] = useMemo(() => {
    if (!event) return [];
    return event.tickets
      .filter((t) => (eventCart[t.name] || 0) > 0)
      .map((t) => ({ name: t.name, qty: eventCart[t.name], price: t.price }));
  }, [event, eventCart]);

  const totalPrice = cartLines.reduce((s, l) => s + l.price * l.qty, 0);

  if (!event) {
    return <Navigate to="/" replace />;
  }
  if (cartLines.length === 0) {
    return <Navigate to={`/events/${event.id}`} replace />;
  }

  const canSubmit =
    buyerName.trim() !== "" &&
    buyerEmail.trim() !== "" &&
    buyerPhone.trim() !== "";

  function confirmOrder() {
    if (!event) return;
    const order: Order = {
      id: generateOrderId(),
      eventId: event.id,
      eventTitle: event.title,
      lines: cartLines,
      total: totalPrice,
      buyerName,
      buyerEmail,
      buyerPhone,
      payment,
      createdAt: new Date().toISOString(),
    };
    addOrder(order);
    navigate(`/order-success/${order.id}`);
  }

  return (
    <main className="mx-auto w-full max-w-[920px] flex-1 px-4 py-8 sm:px-10">
      <Link
        to={`/events/${event.id}`}
        className="mb-5 inline-block text-sm font-semibold text-green"
      >
        ← Quay lại chọn vé
      </Link>
      <h1 className="mb-6 text-[26px] font-extrabold">Thanh toán</h1>

      <div className="flex flex-wrap gap-8">
        <div className="flex min-w-[300px] flex-1 flex-col gap-5">
          <div className="rounded-card border border-border bg-white p-5.5">
            <h3 className="mb-3.5 text-[15px] font-bold">
              Thông tin người mua
            </h3>
            <input
              type="text"
              placeholder="Họ và tên"
              value={buyerName}
              onChange={(e) => setBuyerName(e.target.value)}
              className="mb-3 w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
            />
            <input
              type="email"
              placeholder="Email"
              value={buyerEmail}
              onChange={(e) => setBuyerEmail(e.target.value)}
              className="mb-3 w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
            />
            <input
              type="tel"
              placeholder="Số điện thoại"
              value={buyerPhone}
              onChange={(e) => setBuyerPhone(e.target.value)}
              className="w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green"
            />
          </div>
          <div className="rounded-card border border-border bg-white p-5.5">
            <h3 className="mb-3.5 text-[15px] font-bold">
              Phương thức thanh toán
            </h3>
            {PAYMENT_OPTIONS.map((p) => (
              <PaymentOption
                key={p.key}
                label={p.label}
                active={payment === p.key}
                onSelect={() => setPayment(p.key)}
              />
            ))}
          </div>
        </div>

        <div className="min-w-[280px] flex-1">
          <div className="rounded-card bg-dark p-5.5 text-cream lg:sticky lg:top-24">
            <h3 className="mb-4 text-[15px] font-bold text-gold">
              Đơn hàng — {event.title}
            </h3>
            {cartLines.map((line) => (
              <div
                key={line.name}
                className="flex justify-between border-b border-dark-border py-2 text-[13px]"
              >
                <span>
                  {line.name} × {line.qty}
                </span>
                <span>{formatPrice(line.price * line.qty)}</span>
              </div>
            ))}
            <div className="flex justify-between py-4 pb-5 text-base font-extrabold">
              <span>Tổng</span>
              <span className="text-gold">{formatPrice(totalPrice)}</span>
            </div>
            <Button
              variant="gold"
              className="w-full"
              disabled={!canSubmit}
              onClick={confirmOrder}
            >
              Xác nhận thanh toán
            </Button>
          </div>
        </div>
      </div>
    </main>
  );
}
