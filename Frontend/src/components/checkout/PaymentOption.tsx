interface PaymentOptionProps {
  label: string;
  active: boolean;
  onSelect: () => void;
}

export function PaymentOption({ label, active, onSelect }: PaymentOptionProps) {
  return (
    <div
      onClick={onSelect}
      role="radio"
      aria-checked={active}
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") onSelect();
      }}
      className={`mb-2.5 flex cursor-pointer items-center gap-3 rounded-[10px] border px-3.5 py-3 ${
        active ? "border-green bg-green-tint" : "border-border bg-white"
      }`}
    >
      <div className="flex h-4.5 w-4.5 shrink-0 items-center justify-center rounded-full border-2 border-green">
        {active && <div className="h-2.5 w-2.5 rounded-full bg-green" />}
      </div>
      <span className="text-sm font-semibold">{label}</span>
    </div>
  );
}
