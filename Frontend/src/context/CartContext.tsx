import { createContext, useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";

type EventCart = Record<string, number>; // ticketName -> qty
type CartState = Record<string, EventCart>; // eventId -> EventCart

const STORAGE_KEY = "veluawa_cart";
const EMPTY_EVENT_CART: EventCart = {};

interface CartContextValue {
  cart: CartState;
  getEventCart: (eventId: string) => EventCart;
  incQty: (eventId: string, ticketName: string) => void;
  decQty: (eventId: string, ticketName: string) => void;
  clearEventCart: (eventId: string) => void;
}

export const CartContext = createContext<CartContextValue | null>(null);

function loadCart(): CartState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as CartState) : {};
  } catch {
    return {};
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<CartState>(loadCart);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
  }, [cart]);

  const getEventCart = useCallback(
    (eventId: string) => cart[eventId] || EMPTY_EVENT_CART,
    [cart],
  );

  const incQty = useCallback((eventId: string, ticketName: string) => {
    setCart((s) => {
      const evCart = { ...(s[eventId] || {}) };
      evCart[ticketName] = (evCart[ticketName] || 0) + 1;
      return { ...s, [eventId]: evCart };
    });
  }, []);

  const decQty = useCallback((eventId: string, ticketName: string) => {
    setCart((s) => {
      const evCart = { ...(s[eventId] || {}) };
      evCart[ticketName] = Math.max(0, (evCart[ticketName] || 0) - 1);
      return { ...s, [eventId]: evCart };
    });
  }, []);

  const clearEventCart = useCallback((eventId: string) => {
    setCart((s) => {
      const next = { ...s };
      delete next[eventId];
      return next;
    });
  }, []);

  return (
    <CartContext.Provider
      value={{ cart, getEventCart, incQty, decQty, clearEventCart }}
    >
      {children}
    </CartContext.Provider>
  );
}
