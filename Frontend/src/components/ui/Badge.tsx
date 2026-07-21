import type { ReactNode } from "react";

interface BadgeProps {
  children: ReactNode;
  className?: string;
}

export function Badge({ children, className = "" }: BadgeProps) {
  return (
    <span
      className={`inline-block rounded-pill bg-gold px-3 py-1.5 text-xs font-bold text-dark ${className}`}
    >
      {children}
    </span>
  );
}
