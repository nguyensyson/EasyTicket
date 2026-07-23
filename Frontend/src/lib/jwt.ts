export interface JwtPayload {
  sub: string;
  email?: string;
  preferred_username?: string;
  name?: string;
  exp?: number;
  [claim: string]: unknown;
}

/** Giải mã phần payload của JWT (không xác minh chữ ký — chỉ dùng để đọc claim hiển thị ở client). */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join(""),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}
