import { getAccessToken } from "@/lib/tokenStorage";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8000";

export interface ApiResponse<T> {
  success: boolean;
  errorCode: string | null;
  message: string | null;
  data: T | null;
  traceId: string;
}

export class ApiError extends Error {
  errorCode: string;

  constructor(errorCode: string, message: string) {
    super(message);
    this.errorCode = errorCode;
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const accessToken = getAccessToken();

  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new ApiError("NETWORK_ERROR", "Không thể kết nối tới máy chủ. Vui lòng thử lại.");
  }

  let body: ApiResponse<T> | null = null;
  try {
    body = await res.json();
  } catch {
    // response has no JSON body (e.g. 502 from gateway)
  }

  if (!res.ok || !body || !body.success) {
    throw new ApiError(
      body?.errorCode || String(res.status),
      body?.message || "Đã có lỗi xảy ra. Vui lòng thử lại.",
    );
  }

  return body.data as T;
}
