import type { ButtonHTMLAttributes, ReactNode } from "react";

type Variant = "gold" | "green" | "outline-gold" | "dark";
type Size = "sm" | "md";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  children: ReactNode;
}

const VARIANT_CLASSES: Record<Variant, string> = {
  gold: "bg-gold text-dark hover:bg-gold-hover disabled:bg-[#EDE6D5] disabled:text-[#8f8f86] disabled:cursor-not-allowed",
  green: "bg-green text-cream hover:bg-green-hover disabled:opacity-50 disabled:cursor-not-allowed",
  "outline-gold":
    "bg-transparent border border-gold text-gold hover:bg-gold hover:text-dark",
  dark: "bg-dark text-cream hover:bg-dark-soft",
};

const SIZE_CLASSES: Record<Size, string> = {
  md: "px-6 py-3 text-[15px]",
  sm: "px-3.5 py-2 text-xs",
};

export function Button({
  variant = "gold",
  size = "md",
  className = "",
  children,
  ...rest
}: ButtonProps) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-[10px] font-bold font-sans cursor-pointer transition-colors ${SIZE_CLASSES[size]} ${VARIANT_CLASSES[variant]} ${className}`}
      {...rest}
    >
      {children}
    </button>
  );
}
