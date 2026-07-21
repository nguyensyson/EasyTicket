import { createContext, useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type { Order } from "@/types/event";

const STORAGE_KEY = "veluawa_orders";

interface OrderContextValue {
  orders: Order[];
  addOrder: (order: Order) => void;
  getOrder: (orderId: string) => Order | undefined;
}

export const OrderContext = createContext<OrderContextValue | null>(null);

function loadOrders(): Order[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Order[]) : [];
  } catch {
    return [];
  }
}

export function OrderProvider({ children }: { children: ReactNode }) {
  const [orders, setOrders] = useState<Order[]>(loadOrders);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(orders));
  }, [orders]);

  const addOrder = useCallback((order: Order) => {
    setOrders((s) => [order, ...s]);
  }, []);

  const getOrder = useCallback(
    (orderId: string) => orders.find((o) => o.id === orderId),
    [orders],
  );

  return (
    <OrderContext.Provider value={{ orders, addOrder, getOrder }}>
      {children}
    </OrderContext.Provider>
  );
}
