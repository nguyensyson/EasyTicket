import { useMemo, useState } from "react";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { Plus, Trash2 } from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { useOrganizerEvents } from "@/hooks/useOrganizerEvents";
import { Button } from "@/components/ui/Button";
import type { CategoryKey, EventStatus } from "@/types/event";
import type { OrganizerEventInput } from "@/context/OrganizerEventContext";

const CATEGORY_OPTIONS: { value: CategoryKey; label: string }[] = [
  { value: "concert", label: "Nhạc sống" },
  { value: "sankhau", label: "Sân khấu & Nghệ thuật" },
  { value: "thethao", label: "Thể thao" },
  { value: "hoithao", label: "Hội thảo" },
];

interface TicketRowState {
  name: string;
  price: string;
  totalQuantity: string;
}

const inputClass =
  "w-full rounded-lg border border-border-soft px-3.5 py-2.5 text-sm outline-none focus:border-green";
const labelClass = "mb-1.5 block text-sm font-semibold";

export function OrganizerEventFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const { user } = useAuth();
  const { getById, createEvent, updateEvent } = useOrganizerEvents();

  const existing = id ? getById(id) : undefined;

  const [title, setTitle] = useState(existing?.title ?? "");
  const [description, setDescription] = useState(existing?.description ?? "");
  const [category, setCategory] = useState<CategoryKey>(
    existing?.category ?? "concert",
  );
  const [locationName, setLocationName] = useState(existing?.locationName ?? "");
  const [address, setAddress] = useState(existing?.address ?? "");
  const [startTime, setStartTime] = useState(existing?.startTime ?? "");
  const [endTime, setEndTime] = useState(existing?.endTime ?? "");
  const [ticketTypes, setTicketTypes] = useState<TicketRowState[]>(
    existing && existing.ticketTypes.length > 0
      ? existing.ticketTypes.map((t) => ({
          name: t.name,
          price: String(t.price),
          totalQuantity: String(t.totalQuantity),
        }))
      : [{ name: "", price: "", totalQuantity: "" }],
  );
  const [flashSaleEnabled, setFlashSaleEnabled] = useState(
    Boolean(existing?.flashSale),
  );
  const [flashStart, setFlashStart] = useState(existing?.flashSale?.startAt ?? "");
  const [flashEnd, setFlashEnd] = useState(existing?.flashSale?.endAt ?? "");
  const [errors, setErrors] = useState<string[]>([]);

  const canAccess = useMemo(() => {
    if (!isEdit) return true;
    return Boolean(existing && user && existing.organizerId === user.email);
  }, [isEdit, existing, user]);

  if (!canAccess) {
    return <Navigate to="/organizer/events" replace />;
  }

  function addTicketRow() {
    setTicketTypes((rows) => [...rows, { name: "", price: "", totalQuantity: "" }]);
  }

  function removeTicketRow(index: number) {
    setTicketTypes((rows) => rows.filter((_, i) => i !== index));
  }

  function updateTicketRow(index: number, patch: Partial<TicketRowState>) {
    setTicketTypes((rows) =>
      rows.map((row, i) => (i === index ? { ...row, ...patch } : row)),
    );
  }

  function buildInput(): OrganizerEventInput {
    return {
      title: title.trim(),
      description: description.trim(),
      category,
      locationName: locationName.trim(),
      address: address.trim(),
      startTime,
      endTime,
      ticketTypes: ticketTypes.map((t) => ({
        name: t.name.trim(),
        price: Number(t.price) || 0,
        totalQuantity: Number(t.totalQuantity) || 0,
      })),
      flashSale:
        flashSaleEnabled && flashStart && flashEnd
          ? { startAt: flashStart, endAt: flashEnd }
          : null,
    };
  }

  function validate(input: OrganizerEventInput, status: EventStatus): string[] {
    const errs: string[] = [];
    if (!input.title) errs.push("Tên sự kiện không được để trống.");

    if (status === "PUBLISHED") {
      if (!input.locationName) errs.push("Thiếu thành phố/tỉnh tổ chức.");
      if (!input.address) errs.push("Thiếu địa chỉ cụ thể.");
      if (!input.startTime || !input.endTime) {
        errs.push("Thiếu thời gian bắt đầu/kết thúc sự kiện.");
      } else if (new Date(input.endTime) <= new Date(input.startTime)) {
        errs.push("Thời gian kết thúc phải sau thời gian bắt đầu.");
      }
      if (input.ticketTypes.length === 0) {
        errs.push("Cần ít nhất một loại vé.");
      }
      input.ticketTypes.forEach((t, i) => {
        if (!t.name) errs.push(`Loại vé #${i + 1}: thiếu tên.`);
        if (t.price <= 0) errs.push(`Loại vé #${i + 1}: giá phải lớn hơn 0.`);
        if (t.totalQuantity <= 0) {
          errs.push(`Loại vé #${i + 1}: số lượng phải lớn hơn 0.`);
        }
      });
      if (input.flashSale && new Date(input.flashSale.endAt) <= new Date(input.flashSale.startAt)) {
        errs.push("Flash Sale: giờ kết thúc phải sau giờ bắt đầu.");
      }
    }
    return errs;
  }

  function handleSave(status: EventStatus) {
    if (!user) return;
    const input = buildInput();
    const errs = validate(input, status);
    setErrors(errs);
    if (errs.length > 0) {
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }

    if (isEdit && existing) {
      updateEvent(existing.id, input, status);
    } else {
      createEvent(user.email, user.name, input, status);
    }
    navigate("/organizer/events");
  }

  return (
    <div className="mx-auto max-w-[760px]">
      <h1 className="mb-1 text-2xl font-extrabold">
        {isEdit ? "Sửa sự kiện" : "Tạo sự kiện mới"}
      </h1>
      <p className="mb-6 text-sm text-muted">
        Có thể lưu nháp và hoàn thiện sau, hoặc xuất bản ngay để hiển thị công
        khai trên trang chủ.
      </p>

      {errors.length > 0 && (
        <div className="mb-5 rounded-card border border-[#E0A9A0] bg-[#F4D9D4] p-4">
          <div className="mb-1 text-sm font-bold text-[#A23B2E]">
            Vui lòng kiểm tra lại:
          </div>
          <ul className="list-inside list-disc text-sm text-[#A23B2E]">
            {errors.map((err) => (
              <li key={err}>{err}</li>
            ))}
          </ul>
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
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Ví dụ: Đêm Nhạc Hoàng Hôn — Live Concert"
              />
            </div>
            <div>
              <label className={labelClass}>Mô tả</label>
              <textarea
                className={`${inputClass} min-h-[100px] resize-y`}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Giới thiệu ngắn gọn về sự kiện..."
              />
            </div>
            <div>
              <label className={labelClass}>Danh mục</label>
              <select
                className={inputClass}
                value={category}
                onChange={(e) => setCategory(e.target.value as CategoryKey)}
              >
                {CATEGORY_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2">
              <div>
                <label className={labelClass}>Thành phố/Tỉnh</label>
                <input
                  className={inputClass}
                  value={locationName}
                  onChange={(e) => setLocationName(e.target.value)}
                  placeholder="TP.HCM"
                />
              </div>
              <div>
                <label className={labelClass}>Địa chỉ cụ thể</label>
                <input
                  className={inputClass}
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
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
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                />
              </div>
              <div>
                <label className={labelClass}>Kết thúc</label>
                <input
                  type="datetime-local"
                  className={inputClass}
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="rounded-card border border-border bg-white p-5.5">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-[15px] font-bold">Loại vé</h2>
            <button
              type="button"
              onClick={addTicketRow}
              className="flex cursor-pointer items-center gap-1.5 text-sm font-semibold text-green"
            >
              <Plus className="h-4 w-4" /> Thêm loại vé
            </button>
          </div>
          <div className="flex flex-col gap-3">
            {ticketTypes.map((row, i) => (
              <div
                key={i}
                className="grid grid-cols-1 items-start gap-2.5 sm:grid-cols-[2fr_1fr_1fr_auto]"
              >
                <input
                  className={inputClass}
                  placeholder="Tên loại vé (VD: Vé VIP)"
                  value={row.name}
                  onChange={(e) => updateTicketRow(i, { name: e.target.value })}
                />
                <input
                  type="number"
                  min={0}
                  className={inputClass}
                  placeholder="Giá (đ)"
                  value={row.price}
                  onChange={(e) => updateTicketRow(i, { price: e.target.value })}
                />
                <input
                  type="number"
                  min={0}
                  className={inputClass}
                  placeholder="Số lượng"
                  value={row.totalQuantity}
                  onChange={(e) =>
                    updateTicketRow(i, { totalQuantity: e.target.value })
                  }
                />
                <button
                  type="button"
                  onClick={() => removeTicketRow(i)}
                  disabled={ticketTypes.length === 1}
                  aria-label="Xóa loại vé"
                  className="flex h-[42px] cursor-pointer items-center justify-center rounded-lg border border-border-soft px-3 text-muted hover:bg-cream disabled:cursor-not-allowed disabled:opacity-40"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
          </div>
        </div>

        <div className="rounded-card border border-border bg-white p-5.5">
          <label className="flex cursor-pointer items-center gap-2.5">
            <input
              type="checkbox"
              checked={flashSaleEnabled}
              onChange={(e) => setFlashSaleEnabled(e.target.checked)}
              className="h-4 w-4 accent-green"
            />
            <span className="text-[15px] font-bold">
              Lên lịch Flash Sale cho sự kiện này
            </span>
          </label>
          {flashSaleEnabled && (
            <div className="mt-3.5 grid grid-cols-1 gap-3.5 sm:grid-cols-2">
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
          )}
        </div>

        <div className="flex flex-wrap justify-end gap-3">
          <Button variant="dark" onClick={() => handleSave("DRAFT")}>
            Lưu nháp
          </Button>
          <Button variant="green" onClick={() => handleSave("PUBLISHED")}>
            Xuất bản
          </Button>
        </div>
      </div>
    </div>
  );
}
