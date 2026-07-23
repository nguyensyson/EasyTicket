import { useEffect, useMemo, useState } from "react";
import { EVENTS, getEventById as getStaticEventById } from "@/data/events";
import { eventDtoToItem } from "@/utils/organizerEventAdapter";
import {
  getPublishedEvent,
  listLocations,
  listTicketTypes,
  searchPublishedEvents,
} from "@/services/eventService";
import type { EventItem } from "@/types/event";

/** Sự kiện tĩnh (seed data) + sự kiện PUBLISHED thật từ Event Service — dùng cho trang chủ / tìm kiếm công khai. */
export function useAllEvents(): EventItem[] {
  const [remoteEvents, setRemoteEvents] = useState<EventItem[]>([]);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [page, locations] = await Promise.all([
          searchPublishedEvents({ size: 100 }),
          listLocations(),
        ]);
        const cityNameById = new Map(locations.map((l) => [l.id, l.name]));

        const items = await Promise.all(
          page.content.map(async (event) => {
            const ticketTypes = await listTicketTypes(event.id).catch(() => []);
            return eventDtoToItem(event, ticketTypes, cityNameById.get(event.locationId));
          }),
        );
        if (!cancelled) setRemoteEvents(items);
      } catch {
        if (!cancelled) setRemoteEvents([]);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return useMemo(() => [...EVENTS, ...remoteEvents], [remoteEvents]);
}

/** Tra cứu 1 sự kiện theo id — thử seed data tĩnh trước, rồi tới Event Service (chỉ trả về event PUBLISHED). */
export function useEventLookup(id: string | undefined): EventItem | undefined {
  const [remoteEvent, setRemoteEvent] = useState<EventItem | undefined>(undefined);
  const staticEvent = id ? getStaticEventById(id) : undefined;

  useEffect(() => {
    if (!id || staticEvent) {
      setRemoteEvent(undefined);
      return;
    }
    let cancelled = false;

    async function load() {
      try {
        const [event, ticketTypes, locations] = await Promise.all([
          getPublishedEvent(id as string),
          listTicketTypes(id as string).catch(() => []),
          listLocations().catch(() => []),
        ]);
        const cityName = locations.find((l) => l.id === event.locationId)?.name;
        if (!cancelled) setRemoteEvent(eventDtoToItem(event, ticketTypes, cityName));
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

  return staticEvent ?? remoteEvent;
}
