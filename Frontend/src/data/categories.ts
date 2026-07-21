import type { CategoryKey, CategoryStyle } from "@/types/event";

export const CATEGORY_TABS: { key: string; label: string }[] = [
  { key: "noibat", label: "Nổi bật" },
  { key: "concert", label: "Nhạc sống" },
  { key: "sankhau", label: "Sân khấu & Nghệ thuật" },
  { key: "thethao", label: "Thể thao" },
  { key: "hoithao", label: "Hội thảo" },
];

export const CATEGORY_STYLES: Record<CategoryKey, CategoryStyle> = {
  concert: { bg: "#EFE3C8", stripe: "#D9B26F", label: "Nhạc sống" },
  sankhau: { bg: "#E3ECE7", stripe: "#2D6A4F", label: "Sân khấu" },
  thethao: { bg: "#E8E8E6", stripe: "#161616", label: "Thể thao" },
  hoithao: { bg: "#EDE6D9", stripe: "#D9B26F", label: "Hội thảo" },
};
