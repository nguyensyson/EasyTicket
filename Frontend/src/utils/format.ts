export function formatPrice(amount: number): string {
  return amount.toLocaleString("vi-VN") + "đ";
}

export function formatMinPrice(prices: number[]): string {
  return formatPrice(Math.min(...prices));
}

export function generateOrderId(): string {
  return "VL" + Math.random().toString(36).slice(2, 8).toUpperCase();
}

const MONTHS_LABEL = [
  "Th01", "Th02", "Th03", "Th04", "Th05", "Th06",
  "Th07", "Th08", "Th09", "Th10", "Th11", "Th12",
];

export function formatDateLabel(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const dd = String(d.getDate()).padStart(2, "0");
  return `${dd} ${MONTHS_LABEL[d.getMonth()]}, ${d.getFullYear()}`;
}

export function formatDateTimeLabel(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mm} ${formatDateLabel(iso)}`;
}
