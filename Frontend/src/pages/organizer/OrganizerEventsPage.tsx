import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Pencil, BarChart3, Trash2, Send, Ban } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { useOrganizerEvents } from "@/hooks/useOrganizerEvents";
import { getCategoryLabel } from "@/utils/eventImage";
import { formatDateLabel } from "@/utils/format";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { Button } from "@/components/ui/Button";
import { PillButton } from "@/components/ui/PillButton";
import type { EventStatus } from "@/types/event";

const FILTERS: { key: EventStatus | "ALL"; label: string }[] = [
  { key: "ALL", label: "Tất cả" },
  { key: "DRAFT", label: "Nháp" },
  { key: "PUBLISHED", label: "Đã xuất bản" },
  { key: "CANCELLED", label: "Đã hủy" },
];

export function OrganizerEventsPage() {
  const { user } = useAuth();
  const { getMine, setStatus, removeEvent } = useOrganizerEvents();
  const [filter, setFilter] = useState<EventStatus | "ALL">("ALL");

  const myEvents = useMemo(
    () => (user ? getMine(user.email) : []),
    [user, getMine],
  );

  const filtered = useMemo(
    () =>
      filter === "ALL"
        ? myEvents
        : myEvents.filter((e) => e.status === filter),
    [myEvents, filter],
  );

  function handlePublish(id: string) {
    setStatus(id, "PUBLISHED");
  }

  function handleCancel(id: string) {
    if (window.confirm("Hủy sự kiện này? Sự kiện sẽ không còn hiển thị cho người mua.")) {
      setStatus(id, "CANCELLED");
    }
  }

  function handleDelete(id: string) {
    if (window.confirm("Xóa sự kiện này khỏi danh sách quản lý? Hành động này không thể hoàn tác trên giao diện.")) {
      removeEvent(id);
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
                ({myEvents.filter((e) => e.status === f.key).length})
              </span>
            )}
          </PillButton>
        ))}
      </div>

      {filtered.length === 0 ? (
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
                      {getCategoryLabel(event.category)}
                    </span>
                  </div>
                  <div className="text-base font-bold">{event.title}</div>
                  <div className="text-sm text-muted">
                    {formatDateLabel(event.startTime)} · {event.locationName}{" "}
                    · {event.ticketTypes.length} loại vé
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
                      onClick={() => handlePublish(event.id)}
                    >
                      <Send className="h-3.5 w-3.5" /> Xuất bản
                    </Button>
                  )}
                  {event.status !== "CANCELLED" && (
                    <button
                      onClick={() => handleCancel(event.id)}
                      className="flex cursor-pointer items-center gap-1.5 rounded-[10px] border border-[#E0A9A0] px-3.5 py-2 text-xs font-bold text-[#A23B2E] hover:bg-[#F4D9D4]"
                    >
                      <Ban className="h-3.5 w-3.5" /> Hủy sự kiện
                    </button>
                  )}
                  <button
                    onClick={() => handleDelete(event.id)}
                    aria-label="Xóa sự kiện"
                    className="flex cursor-pointer items-center gap-1.5 rounded-[10px] border border-border-soft px-3 py-2 text-xs font-bold text-muted hover:bg-cream"
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
