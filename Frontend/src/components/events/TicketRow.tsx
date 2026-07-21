import { Minus, Plus } from "lucide-react";
import type { TicketType } from "@/types/event";
import { formatPrice } from "@/utils/format";

interface TicketRowProps {
  ticket: TicketType;
  qty: number;
  onInc: () => void;
  onDec: () => void;
}

export function TicketRow({ ticket, qty, onInc, onDec }: TicketRowProps) {
  return (
    <div className="flex items-center justify-between border-b border-[#EEE7D9] py-3">
      <div>
        <div className="text-sm font-bold">{ticket.name}</div>
        <div className="text-[13px] text-muted">{formatPrice(ticket.price)}</div>
      </div>
      <div className="flex items-center gap-2.5">
        <button
          onClick={onDec}
          disabled={qty === 0}
          aria-label={`Giảm số lượng ${ticket.name}`}
          className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg border border-gold bg-cream text-ink disabled:cursor-not-allowed disabled:opacity-40"
        >
          <Minus className="h-3.5 w-3.5" />
        </button>
        <span className="w-5 text-center font-bold">{qty}</span>
        <button
          onClick={onInc}
          aria-label={`Tăng số lượng ${ticket.name}`}
          className="flex h-7 w-7 cursor-pointer items-center justify-center rounded-lg bg-gold text-dark hover:bg-gold-hover"
        >
          <Plus className="h-3.5 w-3.5" />
        </button>
      </div>
    </div>
  );
}
