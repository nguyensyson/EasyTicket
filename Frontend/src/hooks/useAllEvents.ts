import { useEffect, useMemo, useState } from "react";
import { eventDtoToItem } from "@/utils/organizerEventAdapter";
import { buildCategoryKeyLookup } from "@/utils/eventCategory";
import {
  getPublishedEvent,
  listCategories,
  listLocations,
  listTicketTypes,
  searchPublishedEvents,
} from "@/services/eventService";
import type { EventItem } from "@/types/event";
import type { CategoryDto, EventDto } from "@/types/eventApi";

/** Nhãn category sinh ra ở tầng logic (Event Service, không tồn tại trong bảng categories) đánh dấu
 * sự kiện "nổi bật" — mỗi sự kiện nổi bật xuất hiện 2 lần trong `content`: 1 lần với category thật,
 * 1 lần với category này. */
const FEATURED_CATEGORY_LABEL = "Nổi bật";

/** Gộp các bản ghi trùng `id` do trang đầu tiên có thể chứa cả bản "Nổi bật" (nhãn giả) lẫn bản
 * category thật của cùng 1 sự kiện — trả về DTO gốc (ưu tiên bản category thật) + tập id nổi bật. */
function dedupeFeaturedContent(content: EventDto[]): { events: EventDto[]; featuredIds: Set<string> } {
  const featuredIds = new Set<string>();
  const byId = new Map<string, EventDto>();

  for (const event of content) {
    if (event.category === FEATURED_CATEGORY_LABEL) {
      featuredIds.add(event.id);
      if (!byId.has(event.id)) byId.set(event.id, event);
      continue;
    }
    byId.set(event.id, event);
  }

  return { events: [...byId.values()], featuredIds };
}

/** Sự kiện PUBLISHED thật từ Event Service, kèm danh mục thật (`/api/v1/categories`) để dựng tab
 * danh mục — dùng cho trang chủ / tìm kiếm công khai. Không còn seed data tĩnh. */
export function useAllEvents(): { events: EventItem[]; categories: CategoryDto[] } {
  const [remoteEvents, setRemoteEvents] = useState<EventItem[]>([]);
  const [categories, setCategories] = useState<CategoryDto[]>([]);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [page, locations, categoryList] = await Promise.all([
          searchPublishedEvents({ size: 100 }),
          listLocations(),
          listCategories(),
        ]);
        const cityNameById = new Map(locations.map((l) => [l.id, l.name]));
        const categoryKeyById = buildCategoryKeyLookup(categoryList);
        const { events, featuredIds } = dedupeFeaturedContent(page.content);

        const items = await Promise.all(
          events.map(async (event) => {
            const ticketTypes = await listTicketTypes(event.id).catch(() => []);
            return eventDtoToItem(
              event,
              ticketTypes,
              cityNameById.get(event.locationId),
              categoryKeyById,
              featuredIds.has(event.id),
            );
          }),
        );
        if (!cancelled) {
          setRemoteEvents(items);
          setCategories(categoryList);
        }
      } catch {
        if (!cancelled) {
          setRemoteEvents([]);
          setCategories([]);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const events = useMemo(() => remoteEvents, [remoteEvents]);
  return { events, categories };
}

/** Tra cứu 1 sự kiện theo id qua Event Service (chỉ trả về event PUBLISHED). */
export function useEventLookup(id: string | undefined): EventItem | undefined {
  const [remoteEvent, setRemoteEvent] = useState<EventItem | undefined>(undefined);

  useEffect(() => {
    if (!id) {
      setRemoteEvent(undefined);
      return;
    }
    let cancelled = false;

    async function load() {
      try {
        const [event, ticketTypes, locations, categories] = await Promise.all([
          getPublishedEvent(id as string),
          listTicketTypes(id as string).catch(() => []),
          listLocations().catch(() => []),
          listCategories().catch(() => []),
        ]);
        const cityName = locations.find((l) => l.id === event.locationId)?.name;
        const categoryKeyById = buildCategoryKeyLookup(categories);
        if (!cancelled) {
          setRemoteEvent(eventDtoToItem(event, ticketTypes, cityName, categoryKeyById));
        }
      } catch {
        if (!cancelled) setRemoteEvent(undefined);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  return remoteEvent;
}
