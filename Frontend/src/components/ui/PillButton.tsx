import type { ButtonHTMLAttributes } from "react";

interface PillButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  active?: boolean;
}

export function PillButton({
  active = false,
  className = "",
  ...rest
}: PillButtonProps) {
  return (
    <button
      className={`cursor-pointer whitespace-nowrap rounded-pill border px-4.5 py-2.5 text-sm font-semibold transition-colors ${
        active
          ? "border-gold bg-gold text-dark"
          : "border-[#D8CDB4] bg-white text-[#3a3a3a] hover:border-gold"
      } ${className}`}
      {...rest}
    />
  );
}
