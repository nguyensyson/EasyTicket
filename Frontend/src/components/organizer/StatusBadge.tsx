import type { EventStatus } from "@/types/event";

const STATUS_LABEL: Record<EventStatus, string> = {
  DRAFT: "Nháp",
  PUBLISHED: "Đã xuất bản",
  CANCELLED: "Đã hủy",
};

const STATUS_CLASS: Record<EventStatus, string> = {
  DRAFT: "bg-[#EDE6D5] text-[#6b6b63]",
  PUBLISHED: "bg-green-tint text-green",
  CANCELLED: "bg-[#F4D9D4] text-[#A23B2E]",
};

export function StatusBadge({ status }: { status: EventStatus }) {
  return (
    <span
      className={`inline-block rounded-pill px-2.5 py-1 text-xs font-bold ${STATUS_CLASS[status]}`}
    >
      {STATUS_LABEL[status]}
    </span>
  );
}
