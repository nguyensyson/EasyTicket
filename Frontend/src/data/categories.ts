import type { CategoryKey, CategoryStyle } from "@/types/event";

export const CATEGORY_STYLES: Record<CategoryKey, CategoryStyle> = {
  concert: { bg: "#EFE3C8", stripe: "#D9B26F", label: "Nhạc sống" },
  sankhau: { bg: "#E3ECE7", stripe: "#2D6A4F", label: "Sân khấu" },
  thethao: { bg: "#E8E8E6", stripe: "#161616", label: "Thể thao" },
  hoithao: { bg: "#EDE6D9", stripe: "#D9B26F", label: "Hội thảo" },
};
