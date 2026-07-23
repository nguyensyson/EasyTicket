import { apiRequest } from "@/lib/apiClient";
import type { UserRole } from "@/types/event";
import type { TicketHistoryItemDto } from "@/types/orderApi";

export interface RegisterPayload {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

export interface RegisterResponse {
  id: string;
}

export function register(role: UserRole, payload: RegisterPayload): Promise<RegisterResponse> {
  const path = role === "organizer" ? "/api/v1/users/register/organizer" : "/api/v1/users/register/buyer";
  return apiRequest<RegisterResponse>(path, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  roles: string[];
}

export function login(payload: LoginPayload): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/api/v1/users/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

// Luồng 8 (Buyer) — UserService forward JWT sang Order Service (GET /api/v1/orders/my-tickets) và trả nguyên vẹn.
export function getTicketHistory(): Promise<TicketHistoryItemDto[]> {
  return apiRequest<TicketHistoryItemDto[]>("/api/v1/users/me/ticket-history");
}
