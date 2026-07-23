import { createContext, useCallback, useState } from "react";
import type { ReactNode } from "react";
import type { UserRole } from "@/types/event";
import { decodeJwt } from "@/lib/jwt";
import { getAccessToken, setTokens, clearTokens } from "@/lib/tokenStorage";
import { login as loginRequest } from "@/services/userService";
import { ApiError } from "@/lib/apiClient";

const STORAGE_KEY = "easyticket_user";

export interface AuthUser {
  name: string;
  email: string;
  role: UserRole;
}

interface AuthContextValue {
  user: AuthUser | null;
  login: (username: string, password: string) => Promise<AuthUser>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

function resolveRole(roles: string[]): UserRole {
  return roles.includes("ORGANIZER") ? "organizer" : "buyer";
}

// Backend chỉ trả accessToken/refreshToken/roles (Luồng 2) — name/email lấy từ claim JWT do Keycloak phát hành.
function buildUser(accessToken: string, roles: string[]): AuthUser | null {
  const payload = decodeJwt(accessToken);
  if (!payload) return null;
  const email = payload.email || payload.preferred_username || "";
  const name = payload.name || payload.preferred_username || email.split("@")[0] || "Người dùng";
  return { name, email, role: resolveRole(roles) };
}

function isAccessTokenValid(): boolean {
  const token = getAccessToken();
  if (!token) return false;
  const payload = decodeJwt(token);
  if (!payload?.exp) return true;
  return payload.exp * 1000 > Date.now();
}

function loadStoredUser(): AuthUser | null {
  if (!isAccessTokenValid()) return null;
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser);

  // Luồng 2 — Đăng nhập: delegate sang User Service, User Service delegate tiếp sang Keycloak.
  const login = useCallback(async (username: string, password: string) => {
    const { accessToken, refreshToken, roles } = await loginRequest({ username, password });
    const resolved = buildUser(accessToken, roles);
    if (!resolved) {
      throw new ApiError("INVALID_TOKEN", "Không thể đọc thông tin phiên đăng nhập.");
    }
    setTokens(accessToken, refreshToken);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(resolved));
    setUser(resolved);
    return resolved;
  }, []);

  const logout = useCallback(() => {
    clearTokens();
    localStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
