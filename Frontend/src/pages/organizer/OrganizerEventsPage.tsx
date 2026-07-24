import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Pencil, BarChart3, Trash2, Send, Ban } from "lucide-react";
import { useOrganizerEvents } from "@/hooks/useOrganizerEvents";
import { formatDateLabel } from "@/utils/format";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { Button } from "@/components/ui/Button";
import { PillButton } from "@/components/ui/PillButton";
import { ApiError } from "@/lib/apiClient";
import { deleteEvent, updateEvent } from "@/services/eventService";
import type { EventStatus } from "@/types/event";
import type { EventCategoryCode, EventDto } from "@/types/eventApi";

const FILTERS: { key: EventStatus | "ALL"; label: string }[] = [
  { key: "ALL", label: "Tất cả" },
  { key: "DRAFT", label: "Nháp" },
  { key: "PUBLISHED", label: "Đã xuất bản" },
  { key: "CANCELLED", label: "Đã hủy" },
];

export function OrganizerEventsPage() {
  const { events, loading, error, refetch } = useOrganizerEvents();
  const [filter, setFilter] = useState<EventStatus | "ALL">("ALL");
  const [actionError, setActionError] = useState<string | null>(null);
  const [pendingId, setPendingId] = useState<string | null>(null);

  const filtered = useMemo(
    () => (filter === "ALL" ? events : events.filter((e) => e.status === filter)),
    [events, filter],
  );

  async function changeStatus(event: EventDto, status: EventStatus) {
    setActionError(null);
    setPendingId(event.id);
    try {
      await updateEvent(event.id, {
        title: event.title,
        description: event.description ?? "",
        // TODO: UpdateEventPayload.category cần đổi thành categoryId (UUID) để khớp
        // UpdateEventRequest thật ở backend — bug tồn tại từ trước, không thuộc phạm vi sửa lần này.
        category: event.category as EventCategoryCode,
        locationId: event.locationId,
        location: event.location,
        bannerUrl: event.bannerUrl ?? undefined,
        startTime: event.startTime,
        endTime: event.endTime,
        status,
      });
      await refetch();
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Không thể cập nhật sự kiện.");
    } finally {
      setPendingId(null);
    }
  }

  function handlePublish(event: EventDto) {
    if (
      window.confirm(
        "Xuất bản sự kiện? Sau khi xuất bản, loại vé và flash sale sẽ bị khóa, không thể thêm/sửa nữa.",
      )
    ) {
      changeStatus(event, "PUBLISHED");
    }
  }

  function handleCancel(event: EventDto) {
    if (window.confirm("Hủy sự kiện này? Sự kiện sẽ không còn hiển thị cho người mua.")) {
      changeStatus(event, "CANCELLED");
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm("Xóa sự kiện này? Hành động này không thể hoàn tác.")) return;
    setActionError(null);
    setPendingId(id);
    try {
      await deleteEvent(id);
      await refetch();
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Không thể xóa sự kiện.");
    } finally {
      setPendingId(null);
    }
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-extrabold">Sự kiện của tôi</h1>
        <Link to="/organizer/events/new">
          <Button variant="green">
            <Plus className="h-4 w-4" /> Tạo sự kiện
          </Button>
        </Link>
      </div>

      {(error || actionError) && (
        <div className="mb-5 rounded-card border border-[#E0A9A0] bg-[#F4D9D4] p-4 text-sm text-[#A23B2E]">
          {error || actionError}
        </div>
      )}

      <div className="mb-5 flex flex-wrap gap-2.5">
        {FILTERS.map((f) => (
          <PillButton
            key={f.key}
            active={filter === f.key}
            onClick={() => setFilter(f.key)}
          >
            {f.label}
            {f.key !== "ALL" && (
              <span className="ml-1.5 text-xs opacity-70">
                ({events.filter((e) => e.status === f.key).length})
              </span>
            )}
          </PillButton>
        ))}
      </div>

      {loading ? (
        <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
          Đang tải danh sách sự kiện...
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
          Không có sự kiện nào ở trạng thái này.
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {filtered.map((event) => (
            <div
              key={event.id}
              className="rounded-card border border-border bg-white p-5"
            >
              <div className="mb-3 flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="mb-1.5 flex flex-wrap items-center gap-2">
                    <StatusBadge status={event.status} />
                    <span className="rounded-pill bg-[#EDE6D9] px-2.5 py-1 text-xs font-semibold text-[#5a5a52]">
                      {event.category}
                    </span>
                  </div>
                  <div className="text-base font-bold">{event.title}</div>
                  <div className="text-sm text-muted">
                    {formatDateLabel(event.startTime)} · {event.location}
                  </div>
                </div>

                <div className="flex flex-wrap gap-2">
                  <Link to={`/organizer/events/${event.id}/dashboard`}>
                    <Button variant="outline-gold" size="sm">
                      <BarChart3 className="h-3.5 w-3.5" /> Dashboard
                    </Button>
                  </Link>
                  <Link to={`/organizer/events/${event.id}/edit`}>
                    <Button variant="dark" size="sm">
                      <Pencil className="h-3.5 w-3.5" /> Sửa
                    </Button>
                  </Link>
                  {event.status === "DRAFT" && (
                    <Button
                      variant="green"
                      size="sm"
                      disabled={pendingId === event.id}
                      onClick={() => handlePublish(event)}
                    >
                      <Send className="h-3.5 w-3.5" /> Xuất bản
                    </Button>
                  )}
                  {event.status !== "CANCELLED" && (
                    <button
                      onClick={() => handleCancel(event)}
                      disabled={pendingId === event.id}
                      className="flex cursor-pointer items-center gap-1.5 rounded-[10px] border border-[#E0A9A0] px-3.5 py-2 text-xs font-bold text-[#A23B2E] hover:bg-[#F4D9D4] disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      <Ban className="h-3.5 w-3.5" /> Hủy sự kiện
                    </button>
                  )}
                  <button
                    onClick={() => handleDelete(event.id)}
                    disabled={pendingId === event.id}
                    aria-label="Xóa sự kiện"
                    className="flex cursor-pointer items-center gap-1.5 rounded-[10px] border border-border-soft px-3 py-2 text-xs font-bold text-muted hover:bg-cream disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
