import { CATEGORY_TABS } from "@/data/categories";
import { PillButton } from "@/components/ui/PillButton";

interface CategoryTabsProps {
  active: string;
  onChange: (key: string) => void;
}

export function CategoryTabs({ active, onChange }: CategoryTabsProps) {
  return (
    <div className="flex flex-wrap gap-3">
      {CATEGORY_TABS.map((tab) => (
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
