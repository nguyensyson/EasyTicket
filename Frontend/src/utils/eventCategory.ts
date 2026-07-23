import type { CategoryKey } from "@/types/event";
import type { EventCategoryCode } from "@/types/eventApi";

export const EVENT_CATEGORY_OPTIONS: { value: EventCategoryCode; label: string }[] = [
  { value: "MUSIC", label: "Nhạc sống" },
  { value: "THEATER", label: "Sân khấu & Nghệ thuật" },
  { value: "SPORTS", label: "Thể thao" },
  { value: "WORKSHOP", label: "Hội thảo" },
  { value: "CONFERENCE", label: "Hội nghị" },
  { value: "OTHER", label: "Khác" },
];

const LABEL_BY_CODE: Record<EventCategoryCode, string> = Object.fromEntries(
  EVENT_CATEGORY_OPTIONS.map((o) => [o.value, o.label]),
) as Record<EventCategoryCode, string>;

export function getEventCategoryLabel(category: EventCategoryCode): string {
  return LABEL_BY_CODE[category] ?? category;
}

/** Quy về nhóm hiển thị (tab danh mục) sẵn có ở trang chủ — chỉ dùng để gộp sự kiện thật vào feed demo. */
const CATEGORY_KEY_BY_CODE: Record<EventCategoryCode, CategoryKey> = {
  MUSIC: "concert",
  THEATER: "sankhau",
  SPORTS: "thethao",
  WORKSHOP: "hoithao",
  CONFERENCE: "hoithao",
  OTHER: "concert",
};

export function toCategoryKey(category: EventCategoryCode): CategoryKey {
  return CATEGORY_KEY_BY_CODE[category] ?? "concert";
}
