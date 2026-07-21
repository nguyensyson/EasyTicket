import { createContext, useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type {
  EventStatus,
  FlashSaleDef,
  OrganizerEvent,
  TicketTypeDef,
} from "@/types/event";

const STORAGE_KEY = "veluawa_organizer_events";

export interface OrganizerEventInput {
  title: string;
  description: string;
  category: OrganizerEvent["category"];
  locationName: string;
  address: string;
  startTime: string;
  endTime: string;
  ticketTypes: Omit<TicketTypeDef, "id">[];
  flashSale: FlashSaleDef | null;
}

interface OrganizerEventContextValue {
  events: OrganizerEvent[];
  getById: (id: string) => OrganizerEvent | undefined;
  getMine: (organizerId: string) => OrganizerEvent[];
  createEvent: (
    organizerId: string,
    organizerName: string,
    input: OrganizerEventInput,
    status: EventStatus,
  ) => OrganizerEvent;
  updateEvent: (
    id: string,
    input: OrganizerEventInput,
    status: EventStatus,
  ) => void;
  setStatus: (id: string, status: EventStatus) => void;
  removeEvent: (id: string) => void;
}

export const OrganizerEventContext =
  createContext<OrganizerEventContextValue | null>(null);

function loadEvents(): OrganizerEvent[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as OrganizerEvent[]) : [];
  } catch {
    return [];
  }
}

function generateId(prefix: string): string {
  return `${prefix}_${Math.random().toString(36).slice(2, 10)}`;
}

export function OrganizerEventProvider({ children }: { children: ReactNode }) {
  const [events, setEvents] = useState<OrganizerEvent[]>(loadEvents);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(events));
  }, [events]);

  const getById = useCallback(
    (id: string) => events.find((e) => e.id === id && e.deleteFlag === "ACTIVE"),
    [events],
  );

  const getMine = useCallback(
    (organizerId: string) =>
      events.filter(
        (e) => e.organizerId === organizerId && e.deleteFlag === "ACTIVE",
      ),
    [events],
  );

  const createEvent = useCallback(
    (
      organizerId: string,
      organizerName: string,
      input: OrganizerEventInput,
      status: EventStatus,
    ) => {
      const now = new Date().toISOString();
      const event: OrganizerEvent = {
        id: generateId("oe"),
        organizerId,
        organizerName,
        title: input.title,
        description: input.description,
        category: input.category,
        locationName: input.locationName,
        address: input.address,
        startTime: input.startTime,
        endTime: input.endTime,
        status,
        ticketTypes: input.ticketTypes.map((t) => ({
          ...t,
          id: generateId("tt"),
        })),
        flashSale: input.flashSale,
        deleteFlag: "ACTIVE",
        createdAt: now,
        updatedAt: now,
      };
      setEvents((s) => [event, ...s]);
      return event;
    },
    [],
  );

  const updateEvent = useCallback(
    (id: string, input: OrganizerEventInput, status: EventStatus) => {
      setEvents((s) =>
        s.map((e) => {
          if (e.id !== id) return e;
          // Giữ id vé cũ theo tên để không làm mất số lượng đã bán trong giỏ hàng đang mở của buyer.
          const ticketTypes = input.ticketTypes.map((t) => {
            const existing = e.ticketTypes.find((old) => old.name === t.name);
            return { ...t, id: existing?.id ?? generateId("tt") };
          });
          return {
            ...e,
            title: input.title,
            description: input.description,
            category: input.category,
            locationName: input.locationName,
            address: input.address,
            startTime: input.startTime,
            endTime: input.endTime,
            ticketTypes,
            flashSale: input.flashSale,
            status,
            updatedAt: new Date().toISOString(),
          };
        }),
      );
    },
    [],
  );

  const setStatus = useCallback((id: string, status: EventStatus) => {
    setEvents((s) =>
      s.map((e) =>
        e.id === id
          ? { ...e, status, updatedAt: new Date().toISOString() }
          : e,
      ),
    );
  }, []);

  const removeEvent = useCallback((id: string) => {
    // Soft delete — không xoá vật lý, chỉ đánh dấu delete_flag = DELETED.
    setEvents((s) =>
      s.map((e) =>
        e.id === id
          ? { ...e, deleteFlag: "DELETED", updatedAt: new Date().toISOString() }
          : e,
      ),
    );
  }, []);

  return (
    <OrganizerEventContext.Provider
      value={{
        events,
        getById,
        getMine,
        createEvent,
        updateEvent,
        setStatus,
        removeEvent,
      }}
    >
      {children}
    </OrganizerEventContext.Provider>
  );
}
