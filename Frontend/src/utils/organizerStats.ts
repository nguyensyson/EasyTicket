export type FlashSaleRuntimeStatus = "SCHEDULED" | "ACTIVE" | "ENDED";

export function getFlashSaleStatus(
  flashSale: { startAt: string; endAt: string } | null | undefined,
): FlashSaleRuntimeStatus | null {
  if (!flashSale) return null;
  const now = Date.now();
  const start = new Date(flashSale.startAt).getTime();
  const end = new Date(flashSale.endAt).getTime();
  if (now < start) return "SCHEDULED";
  if (now > end) return "ENDED";
  return "ACTIVE";
}
