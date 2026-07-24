import { useEffect, useState } from "react";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { Ban, Plus, Send } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/organizer/StatusBadge";
import { ApiError } from "@/lib/apiClient";
import {
  createEvent,
  createFlashSale,
  createTicketType,
  getFlashSale,
  getManagedEvent,
  listLocations,
  listTicketTypes,
  updateEvent,
  updateTicketType,
} from "@/services/eventService";
import { EVENT_CATEGORY_OPTIONS } from "@/utils/eventCategory";
import { getFlashSaleStatus } from "@/utils/organizerStats";
import { formatDateTimeLabel } from "@/utils/format";
import type {
  EventCategoryCode,
  EventDto,
  FlashSaleDto,
  LocationDto,
  TicketTypeDto,
  UpdateEventPayload,
} from "@/types/eventApi";
import type { EventStatus } from "@/types/event";

const inputClass =
  "w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green";
const labelClass = "mb-1.5 block text-sm font-semibold";

interface InfoForm {
  title: string;
  description: string;
  category: EventCategoryCode;
  locationId: string;
  location: string;
  bannerUrl: string;
  startTime: string;
  endTime: string;
}

const EMPTY_FORM: InfoForm = {
  title: "",
  description: "",
  category: "MUSIC",
  locationId: "",
  location: "",
  bannerUrl: "",
  startTime: "",
  endTime: "",
};

function toForm(event: EventDto): InfoForm {
  return {
    title: event.title,
    description: event.description ?? "",
    // TODO: form category dùng EventCategoryCode cũ trong khi EventDto.category thật là tên danh
    // mục (name) — bug categoryId tồn tại từ trước ở luồng tạo/sửa event, không thuộc phạm vi sửa lần này.
    category: event.category as EventCategoryCode,
    locationId: event.locationId,
    location: event.location,
    bannerUrl: event.bannerUrl ?? "",
    startTime: event.startTime,
    endTime: event.endTime,
  };
}

function buildPayload(form: InfoForm, status: EventStatus): UpdateEventPayload {
  return {
    title: form.title.trim(),
    description: form.description.trim(),
    category: form.category,
    locationId: form.locationId,
    location: form.location.trim(),
    bannerUrl: form.bannerUrl.trim() || undefined,
    startTime: form.startTime,
    endTime: form.endTime,
    status,
  };
}

function validateForm(form: InfoForm): string[] {
  const errs: string[] = [];
  if (!form.title.trim()) errs.push("Tên sự kiện không được để trống.");
  if (!form.locationId) errs.push("Vui lòng chọn thành phố/tỉnh.");
  if (!form.location.trim()) errs.push("Thiếu địa chỉ cụ thể.");
  if (!form.startTime || !form.endTime) {
    errs.push("Thiếu thời gian bắt đầu/kết thúc sự kiện.");
  } else if (new Date(form.endTime) <= new Date(form.startTime)) {
    errs.push("Thời gian kết thúc phải sau thời gian bắt đầu.");
  }
  return errs;
}

interface NewTicketRow {
  name: string;
  price: string;
  totalQuantity: string;
}

export function OrganizerEventFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [loading, setLoading] = useState(isEdit);
  const [accessDenied, setAccessDenied] = useState(false);
  const [event, setEvent] = useState<EventDto | null>(null);
  const [form, setForm] = useState<InfoForm>(EMPTY_FORM);
  const [locations, setLocations] = useState<LocationDto[]>([]);
  const [ticketTypes, setTicketTypes] = useState<TicketTypeDto[]>([]);
  const [flashSale, setFlashSale] = useState<FlashSaleDto | null>(null);

  const [infoErrors, setInfoErrors] = useState<string[]>([]);
  const [savingInfo, setSavingInfo] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const [newTicket, setNewTicket] = useState<NewTicketRow>({ name: "", price: "", totalQuantity: "" });
  const [savingTicketId, setSavingTicketId] = useState<string | null>(null);
  const [ticketError, setTicketError] = useState<string | null>(null);

  const [flashStart, setFlashStart] = useState("");
  const [flashEnd, setFlashEnd] = useState("");
  const [savingFlashSale, setSavingFlashSale] = useState(false);
  const [flashError, setFlashError] = useState<string | null>(null);

  useEffect(() => {
    listLocations()
      .then(setLocations)
      .catch(() => setLocations([]));
  }, []);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    setLoading(true);
    Promise.all([
      getManagedEvent(id),
      listTicketTypes(id).catch(() => []),
      getFlashSale(id).catch(() => null),
    ])
      .then(([eventDto, types, flash]) => {
        if (cancelled) return;
        setEvent(eventDto);
        setForm(toForm(eventDto));
        setTicketTypes(types);
        setFlashSale(flash);
      })
      .catch((err) => {
        if (!cancelled && err instanceof ApiError) setAccessDenied(true);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (accessDenied) {
    return <Navigate to="/organizer/events" replace />;
  }

  if (isEdit && loading) {
    return (
      <div className="rounded-card border border-border bg-white py-15 text-center text-[15px] text-[#8a8a80]">
        Đang tải...
      </div>
    );
  }

  const isDraft = !event || event.status === "DRAFT";

  async function handleSaveInfo() {
    const errs = validateForm(form);
    setInfoErrors(errs);
    setActionError(null);
    if (errs.length > 0) {
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }

    setSavingInfo(true);
    try {
      if (event) {
        const updated = await updateEvent(event.id, buildPayload(form, event.status));
        setEvent(updated);
        setForm(toForm(updated));
      } else {
        const created = await createEvent({
          title: form.title.trim(),
          description: form.description.trim(),
          category: form.category,
          locationId: form.locationId,
          location: form.location.trim(),
          bannerUrl: form.bannerUrl.trim() || undefined,
          startTime: form.startTime,
          endTime: form.endTime,
        });
        navigate(`/organizer/events/${created.id}/edit`, { replace: true });
      }
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Không thể lưu sự kiện.");
    } finally {
      setSavingInfo(false);
    }
  }

  async function handlePublish() {
    if (!event) return;
    if (ticketTypes.length === 0) {
      setActionError("Cần ít nhất một loại vé trước khi xuất bản.");
      return;
    }
    const errs = validateForm(form);
    setInfoErrors(errs);
    if (errs.length > 0) {
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }
    if (
      !window.confirm(
        "Xuất bản sự kiện? Sau khi xuất bản, loại vé và flash sale sẽ bị khóa, không thể thêm/sửa nữa.",
      )
    ) {
      return;
    }

    setPublishing(true);
    setActionError(null);
    try {
      const updated = await updateEvent(event.id, buildPayload(form, "PUBLISHED"));
      setEvent(updated);
      setForm(toForm(updated));
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Không thể xuất bản sự kiện.");
    } finally {
      setPublishing(false);
    }
  }

  async function handleCancelEvent() {
    if (!event) return;
    if (!window.confirm("Hủy sự kiện này? Sự kiện sẽ không còn hiển thị cho người mua.")) return;

    setCancelling(true);
    setActionError(null);
    try {
      const updated = await updateEvent(event.id, buildPayload(form, "CANCELLED"));
      setEvent(updated);
      setForm(toForm(updated));
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Không thể hủy sự kiện.");
    } finally {
      setCancelling(false);
    }
  }

  async function handleAddTicketType() {
    if (!event) return;
    const price = Number(newTicket.price);
    const totalQuantity = Number(newTicket.totalQuantity);
    if (!newTicket.name.trim() || !(price >= 0) || !(totalQuantity > 0)) {
      setTicketError("Điền đầy đủ tên, giá (≥0) và số lượng (>0) cho loại vé.");
      return;
    }

    setSavingTicketId("new");
    setTicketError(null);
    try {
      const created = await createTicketType(event.id, {
        name: newTicket.name.trim(),
        price,
        totalQuantity,
      });
      setTicketTypes((rows) => [...rows, created]);
      setNewTicket({ name: "", price: "", totalQuantity: "" });
    } catch (err) {
      setTicketError(err instanceof ApiError ? err.message : "Không thể thêm loại vé.");
    } finally {
      setSavingTicketId(null);
    }
  }

  async function handleUpdateTicketType(ticket: TicketTypeDto) {
    if (!event) return;
    if (!ticket.name.trim() || !(ticket.price >= 0) || !(ticket.totalQuantity > 0)) {
      setTicketError("Điền đầy đủ tên, giá (≥0) và số lượng (>0) cho loại vé.");
      return;
    }

    setSavingTicketId(ticket.id);
    setTicketError(null);
    try {
      const updated = await updateTicketType(event.id, ticket.id, {
        name: ticket.name.trim(),
        price: ticket.price,
        totalQuantity: ticket.totalQuantity,
      });
      setTicketTypes((rows) => rows.map((r) => (r.id === updated.id ? updated : r)));
    } catch (err) {
      setTicketError(err instanceof ApiError ? err.message : "Không thể lưu loại vé.");
    } finally {
      setSavingTicketId(null);
    }
  }

  function patchLocalTicketType(id: string, patch: Partial<TicketTypeDto>) {
    setTicketTypes((rows) => rows.map((r) => (r.id === id ? { ...r, ...patch } : r)));
  }

  async function handleCreateFlashSale() {
    if (!event) return;
    if (!flashStart || !flashEnd) {
      setFlashError("Chọn đầy đủ giờ bắt đầu và kết thúc.");
      return;
    }
    if (new Date(flashEnd) <= new Date(flashStart)) {
      setFlashError("Giờ kết thúc phải sau giờ bắt đầu.");
      return;
    }

    setSavingFlashSale(true);
    setFlashError(null);
    try {
      const created = await createFlashSale(event.id, { startAt: flashStart, endAt: flashEnd });
      setFlashSale(created);
    } catch (err) {
      setFlashError(err instanceof ApiError ? err.message : "Không thể lên lịch Flash Sale.");
    } finally {
      setSavingFlashSale(false);
    }
  }

  const flashStatus = getFlashSaleStatus(flashSale);

  return (
    <div className="mx-auto max-w-[760px]">
      <div className="mb-1 flex flex-wrap items-center gap-2.5">
        <h1 className="text-2xl font-extrabold">
          {isEdit ? "Sửa sự kiện" : "Tạo sự kiện mới"}
        </h1>
        {event && <StatusBadge status={event.status} />}
      </div>
      <p className="mb-6 text-sm text-muted">
        {event
          ? "Thông tin cơ bản có thể sửa bất cứ lúc nào. Loại vé và Flash Sale chỉ chỉnh được khi sự kiện còn ở trạng thái Nháp."
          : "Tạo sự kiện ở trạng thái nháp trước, sau đó thêm loại vé và (tuỳ chọn) lên lịch flash sale, rồi mới xuất bản."}
      </p>

      {(infoErrors.length > 0 || actionError) && (
        <div className="mb-5 rounded-card border border-[#E0A9A0] bg-[#F4D9D4] p-4">
          {actionError && (
            <div className="mb-1 text-sm font-bold text-[#A23B2E]">{actionError}</div>
          )}
          {infoErrors.length > 0 && (
            <ul className="list-inside list-disc text-sm text-[#A23B2E]">
              {infoErrors.map((err) => (
                <li key={err}>{err}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div className="flex flex-col gap-5">
        <div className="rounded-card border border-border bg-white p-5.5">
          <h2 className="mb-4 text-[15px] font-bold">Thông tin sự kiện</h2>
          <div className="flex flex-col gap-3.5">
            <div>
              <label className={labelClass}>Tên sự kiện</label>
              <input
                className={inputClass}
                value={form.title}
                onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                placeholder="Ví dụ: Đêm Nhạc Hoàng Hôn — Live Concert"
              />
            </div>
            <div>
              <label className={labelClass}>Mô tả</label>
              <textarea
                className={`${inputClass} min-h-[100px] resize-y`}
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                placeholder="Giới thiệu ngắn gọn về sự kiện..."
              />
            </div>
            <div>
              <label className={labelClass}>Danh mục</label>
              <select
                className={inputClass}
                value={form.category}
                onChange={(e) =>
                  setForm((f) => ({ ...f, category: e.target.value as EventCategoryCode }))
                }
              >
                {EVENT_CATEGORY_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2">
              <div>
                <label className={labelClass}>Thành phố/Tỉnh</label>
                <select
                  className={inputClass}
                  value={form.locationId}
                  onChange={(e) => setForm((f) => ({ ...f, locationId: e.target.value }))}
                >
                  <option value="">— Chọn —</option>
                  {locations.map((loc) => (
                    <option key={loc.id} value={loc.id}>
                      {loc.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className={labelClass}>Địa chỉ cụ thể</label>
                <input
                  className={inputClass}
                  value={form.location}
                  onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))}
                  placeholder="SVĐ Quân Khu 7"
                />
              </div>
            </div>
            <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2">
              <div>
                <label className={labelClass}>Bắt đầu</label>
                <input
                  type="datetime-local"
                  className={inputClass}
                  value={form.startTime}
                  onChange={(e) => setForm((f) => ({ ...f, startTime: e.target.value }))}
                />
              </div>
              <div>
                <label className={labelClass}>Kết thúc</label>
                <input
                  type="datetime-local"
                  className={inputClass}
                  value={form.endTime}
                  onChange={(e) => setForm((f) => ({ ...f, endTime: e.target.value }))}
                />
              </div>
            </div>
          </div>
          <div className="mt-4.5 flex flex-wrap justify-end gap-3">
            {event?.status === "PUBLISHED" && (
              <button
                type="button"
                onClick={handleCancelEvent}
                disabled={cancelling}
                className="flex cursor-pointer items-center gap-1.5 rounded-[10px] border border-[#E0A9A0] px-3.5 py-2 text-xs font-bold text-[#A23B2E] hover:bg-[#F4D9D4] disabled:cursor-not-allowed disabled:opacity-50"
              >
                <Ban className="h-3.5 w-3.5" /> Hủy sự kiện
              </button>
            )}
            <Button variant="dark" onClick={handleSaveInfo} disabled={savingInfo}>
              {event ? "Lưu thông tin" : "Tạo sự kiện (nháp)"}
            </Button>
          </div>
        </div>

        {event && (
          <div className="rounded-card border border-border bg-white p-5.5">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-[15px] font-bold">Loại vé</h2>
            </div>

            {ticketError && (
              <div className="mb-3 text-sm font-semibold text-[#A23B2E]">{ticketError}</div>
            )}

            {ticketTypes.length === 0 && !isDraft && (
              <div className="py-4 text-center text-sm text-[#8a8a80]">
                Sự kiện chưa có loại vé nào.
              </div>
            )}

            <div className="flex flex-col gap-3">
              {ticketTypes.map((row) =>
                isDraft ? (
                  <div
                    key={row.id}
                    className="grid grid-cols-1 items-start gap-2.5 sm:grid-cols-[2fr_1fr_1fr_auto]"
                  >
                    <input
                      className={inputClass}
                      value={row.name}
                      onChange={(e) => patchLocalTicketType(row.id, { name: e.target.value })}
                    />
                    <input
                      type="number"
                      min={0}
                      className={inputClass}
                      value={row.price}
                      onChange={(e) =>
                        patchLocalTicketType(row.id, { price: Number(e.target.value) })
                      }
                    />
                    <input
                      type="number"
                      min={1}
                      className={inputClass}
                      value={row.totalQuantity}
                      onChange={(e) =>
                        patchLocalTicketType(row.id, { totalQuantity: Number(e.target.value) })
                      }
                    />
                    <Button
                      variant="outline-gold"
                      size="sm"
                      disabled={savingTicketId === row.id}
                      onClick={() => handleUpdateTicketType(row)}
                    >
                      Lưu
                    </Button>
                  </div>
                ) : (
                  <div
                    key={row.id}
                    className="flex items-center justify-between rounded-lg border border-border-soft px-3.5 py-2.5"
                  >
                    <span className="text-sm font-semibold">{row.name}</span>
                    <span className="text-sm text-muted">
                      {row.price.toLocaleString("vi-VN")}đ · {row.totalQuantity} vé
                    </span>
                  </div>
                ),
              )}
            </div>

            {isDraft ? (
              <div className="mt-3.5 grid grid-cols-1 items-start gap-2.5 border-t border-border-soft pt-3.5 sm:grid-cols-[2fr_1fr_1fr_auto]">
                <input
                  className={inputClass}
                  placeholder="Tên loại vé (VD: Vé VIP)"
                  value={newTicket.name}
                  onChange={(e) => setNewTicket((s) => ({ ...s, name: e.target.value }))}
                />
                <input
                  type="number"
                  min={0}
                  className={inputClass}
                  placeholder="Giá (đ)"
                  value={newTicket.price}
                  onChange={(e) => setNewTicket((s) => ({ ...s, price: e.target.value }))}
                />
                <input
                  type="number"
                  min={0}
                  className={inputClass}
                  placeholder="Số lượng"
                  value={newTicket.totalQuantity}
                  onChange={(e) => setNewTicket((s) => ({ ...s, totalQuantity: e.target.value }))}
                />
                <Button
                  variant="green"
                  size="sm"
                  disabled={savingTicketId === "new"}
                  onClick={handleAddTicketType}
                >
                  <Plus className="h-3.5 w-3.5" /> Thêm
                </Button>
              </div>
            ) : (
              <p className="mt-3.5 text-xs text-muted">
                Không thể thêm/sửa loại vé sau khi sự kiện đã xuất bản hoặc bị hủy.
              </p>
            )}
          </div>
        )}

        {event && (
          <div className="rounded-card border border-border bg-white p-5.5">
            <h2 className="mb-1 text-[15px] font-bold">Flash Sale</h2>

            {flashSale ? (
              <div className="mt-3 flex flex-wrap items-center justify-between gap-2 rounded-lg border border-border-soft px-3.5 py-3">
                <div className="text-sm">
                  <span className="font-semibold">
                    {formatDateTimeLabel(flashSale.startAt)} → {formatDateTimeLabel(flashSale.endAt)}
                  </span>
                </div>
                <span className="rounded-pill bg-green-tint px-2.5 py-1 text-xs font-bold text-green">
                  {flashStatus === "SCHEDULED"
                    ? "Chưa bắt đầu"
                    : flashStatus === "ACTIVE"
                      ? "Đang diễn ra"
                      : "Đã kết thúc"}
                </span>
              </div>
            ) : isDraft ? (
              <>
                <p className="mb-3.5 text-sm text-muted">
                  Mỗi sự kiện chỉ lên lịch được một lần — không thể sửa sau khi đã lưu.
                </p>
                {flashError && (
                  <div className="mb-3 text-sm font-semibold text-[#A23B2E]">{flashError}</div>
                )}
                <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2">
                  <div>
                    <label className={labelClass}>Bắt đầu mở bán</label>
                    <input
                      type="datetime-local"
                      className={inputClass}
                      value={flashStart}
                      onChange={(e) => setFlashStart(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className={labelClass}>Kết thúc mở bán</label>
                    <input
                      type="datetime-local"
                      className={inputClass}
                      value={flashEnd}
                      onChange={(e) => setFlashEnd(e.target.value)}
                    />
                  </div>
                </div>
                <div className="mt-3.5 flex justify-end">
                  <Button variant="dark" size="sm" disabled={savingFlashSale} onClick={handleCreateFlashSale}>
                    Lên lịch Flash Sale
                  </Button>
                </div>
              </>
            ) : (
              <p className="mt-2 text-xs text-muted">
                Sự kiện đã xuất bản mà chưa lên lịch Flash Sale — không thể cấu hình thêm.
              </p>
            )}
          </div>
        )}

        {event && isDraft && (
          <div className="flex flex-wrap justify-end gap-3">
            <Button
              variant="green"
              disabled={publishing || ticketTypes.length === 0}
              onClick={handlePublish}
              title={ticketTypes.length === 0 ? "Cần ít nhất một loại vé" : undefined}
            >
              <Send className="h-4 w-4" /> Xuất bản
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
