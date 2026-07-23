import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/apiClient";
import { listMyEvents } from "@/services/eventService";
import type { EventDto } from "@/types/eventApi";

/** Toàn bộ sự kiện (mọi trạng thái) của Organizer đang đăng nhập — GET /api/v1/events/mine. */
export function useOrganizerEvents() {
  const [events, setEvents] = useState<EventDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listMyEvents();
      setEvents(data);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Không thể tải danh sách sự kiện.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { events, loading, error, refetch };
}
