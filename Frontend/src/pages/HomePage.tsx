import { useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useAllEvents } from "@/hooks/useAllEvents";
import { HeroCarousel } from "@/components/events/HeroCarousel";
import { CategoryTabs } from "@/components/events/CategoryTabs";
import { EventCard } from "@/components/events/EventCard";

export function HomePage() {
  const [activeCategory, setActiveCategory] = useState("noibat");
  const [searchParams] = useSearchParams();
  const searchQuery = searchParams.get("q") || "";
  const allEvents = useAllEvents();

  const featuredEvents = useMemo(
    () => allEvents.filter((e) => e.featured),
    [allEvents],
  );

  const filteredEvents = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();

    // Có từ khóa tìm kiếm: tìm trên toàn bộ sự kiện, bỏ qua tab danh mục đang
    // chọn — nếu không, sự kiện không "nổi bật" (vd. vừa được Organizer xuất
    // bản) sẽ không thể tìm thấy khi tab mặc định "Nổi bật" đang active.
    if (q) {
      return allEvents.filter(
        (e) =>
          e.title.toLowerCase().includes(q) ||
          e.venue.toLowerCase().includes(q) ||
          e.city.toLowerCase().includes(q),
      );
    }

    return activeCategory === "noibat"
      ? allEvents.filter((e) => e.featured)
      : allEvents.filter((e) => e.category === activeCategory);
  }, [allEvents, activeCategory, searchQuery]);

  return (
    <main className="flex-1">
      <HeroCarousel events={featuredEvents} />

      <section className="px-4 pb-2 pt-9 sm:px-10">
        <CategoryTabs active={activeCategory} onChange={setActiveCategory} />
      </section>

      <section className="px-4 py-5 pb-15 sm:px-10">
        {searchQuery && (
          <p className="mb-4 text-sm text-muted">
            Kết quả tìm kiếm cho <strong>"{searchQuery}"</strong>
          </p>
        )}
        {filteredEvents.length > 0 ? (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {filteredEvents.map((event) => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>
        ) : (
          <div className="py-15 text-center text-[15px] text-[#8a8a80]">
            Không tìm thấy sự kiện phù hợp.
          </div>
        )}
      </section>
    </main>
  );
}
