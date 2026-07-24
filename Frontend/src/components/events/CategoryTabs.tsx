import { PillButton } from "@/components/ui/PillButton";

export const FEATURED_TAB_KEY = "noibat";

export interface CategoryTab {
  key: string;
  label: string;
}

interface CategoryTabsProps {
  categories: CategoryTab[];
  active: string;
  onChange: (key: string) => void;
}

/** Tab "Nổi bật" luôn cố định, các tab còn lại lấy từ danh mục thật (`/api/v1/categories`). */
export function CategoryTabs({ categories, active, onChange }: CategoryTabsProps) {
  const tabs: CategoryTab[] = [
    { key: FEATURED_TAB_KEY, label: "Nổi bật" },
    ...categories,
  ];

  return (
    <div className="flex flex-wrap gap-3">
      {tabs.map((tab) => (
        <PillButton
          key={tab.key}
          active={tab.key === active}
          onClick={() => onChange(tab.key)}
        >
          {tab.label}
        </PillButton>
      ))}
    </div>
  );
}
