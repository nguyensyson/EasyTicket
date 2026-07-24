import type { CategoryKey } from "@/types/event";
import type { CategoryDto, EventCategoryCode } from "@/types/eventApi";

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

/** Từ khoá nhận diện tab hiển thị dựa trên tên danh mục thật (CategoryDto.name) — dùng để gộp
 * sự kiện thật (categoryId trỏ tới bảng categories) vào 4 tab danh mục sẵn có ở trang chủ. */
const CATEGORY_KEY_KEYWORDS: [RegExp, CategoryKey][] = [
  [/nhạc|music|concert/i, "concert"],
  [/sân khấu|nghệ thuật|theater|theatre/i, "sankhau"],
  [/thể thao|sport/i, "thethao"],
  [/hội thảo|hội nghị|triển lãm|workshop|conference|exhibition/i, "hoithao"],
];

function classifyCategoryName(name: string): CategoryKey {
  const match = CATEGORY_KEY_KEYWORDS.find(([pattern]) => pattern.test(name));
  return match ? match[1] : "concert";
}

/** Gọi 1 lần với danh sách danh mục thật từ `/api/v1/categories`, trả về map categoryId → tab
 * danh mục ở trang chủ. Dùng thay cho `toCategoryKey` (vốn chỉ khớp với EventCategoryCode cũ, không
 * còn phản ánh đúng dữ liệu category thật trả về từ Event Service). */
export function buildCategoryKeyLookup(categories: CategoryDto[]): Map<string, CategoryKey> {
  return new Map(categories.map((c) => [c.id, classifyCategoryName(c.name)]));
}
