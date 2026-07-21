import type { CSSProperties } from "react";
import { CATEGORY_STYLES } from "@/data/categories";
import type { CategoryKey } from "@/types/event";

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
