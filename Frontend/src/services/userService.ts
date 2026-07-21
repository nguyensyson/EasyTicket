import { apiRequest } from "@/lib/apiClient";
import type { UserRole } from "@/types/event";

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
