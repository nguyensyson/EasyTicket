import type { LucideIcon } from "lucide-react";

interface StatCardProps {
  icon: LucideIcon;
  label: string;
  value: string;
  hint?: string;
}

export function StatCard({ icon: Icon, label, value, hint }: StatCardProps) {
  return (
    <div className="rounded-card border border-border bg-white p-5">
      <div className="mb-3 flex items-center gap-2.5">
        <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-green-tint text-green">
          <Icon className="h-4.5 w-4.5" />
        </span>
        <span className="text-sm font-semibold text-muted">{label}</span>
      </div>
      <div className="text-2xl font-extrabold text-ink">{value}</div>
      {hint && <div className="mt-1 text-xs text-muted">{hint}</div>}
    </div>
  );
}
