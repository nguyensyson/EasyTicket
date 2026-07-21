import { useMemo } from "react";
import { EVENTS, getEventById as getStaticEventById } from "@/data/events";
import { useOrganizerEvents } from "@/hooks/useOrganizerEvents";
import { organizerEventToItem } from "@/utils/organizerEventAdapter";
import type { EventItem } from "@/types/event";

/** Sự kiện tĩnh (seed data) + sự kiện Organizer đã PUBLISHED — dùng cho trang chủ / tìm kiếm công khai. */
export function useAllEvents(): EventItem[] {
  const { events } = useOrganizerEvents();
  return useMemo(() => {
    const published = events
      .filter((e) => e.status === "PUBLISHED" && e.deleteFlag === "ACTIVE")
      .map(organizerEventToItem);
    return [...EVENTS, ...published];
  }, [events]);
}

/**
 * Tra cứu 1 sự kiện theo id, gộp cả hai nguồn. Không lọc theo status — cho phép
 * Organizer xem trước sự kiện DRAFT qua link trực tiếp, dù chưa hiển thị công khai.
 */
export function useEventLookup(id: string | undefined): EventItem | undefined {
  const { getById } = useOrganizerEvents();
  return useMemo(() => {
    if (!id) return undefined;
    const staticEvent = getStaticEventById(id);
    if (staticEvent) return staticEvent;
    const oe = getById(id);
    return oe ? organizerEventToItem(oe) : undefined;
  }, [id, getById]);
}
