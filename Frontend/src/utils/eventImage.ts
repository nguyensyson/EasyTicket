import type { CSSProperties } from "react";
import { CATEGORY_STYLES } from "@/data/categories";
import type { CategoryKey, EventItem } from "@/types/event";

export function getEventImageStyle(category: CategoryKey): CSSProperties {
  const c = CATEGORY_STYLES[category];
  return {
    background: c.bg,
    backgroundImage: `repeating-linear-gradient(135deg, ${c.stripe}33, ${c.stripe}33 12px, transparent 12px, transparent 24px)`,
  };
}

export function getCategoryLabel(category: CategoryKey): string {
  return CATEGORY_STYLES[category].label;
}

/** Nhãn danh mục hiển thị trên thẻ/banner sự kiện — ưu tiên tên danh mục thật (categoryLabel, từ
 * Event Service) thay vì nhãn nhóm cũ (chỉ gồm 4 nhóm, không phản ánh hết danh mục thật trong DB). */
export function getEventCategoryDisplayLabel(event: EventItem): string {
  return event.categoryLabel ?? getCategoryLabel(event.category);
}
