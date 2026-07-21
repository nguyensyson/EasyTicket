import { createContext, useCallback, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type { UserRole } from "@/types/event";

const STORAGE_KEY = "veluawa_user";
const DIRECTORY_KEY = "veluawa_user_directory";

export interface AuthUser {
  name: string;
  email: string;
  role: UserRole;
}

type Directory = Record<string, { name: string; role: UserRole }>;

interface AuthContextValue {
  user: AuthUser | null;
  login: (email: string, role: UserRole, name?: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

function loadUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    return null;
  }
}

function loadDirectory(): Directory {
  try {
    const raw = localStorage.getItem(DIRECTORY_KEY);
    return raw ? (JSON.parse(raw) as Directory) : {};
  } catch {
    return {};
  }
}

function saveDirectory(dir: Directory) {
  localStorage.setItem(DIRECTORY_KEY, JSON.stringify(dir));
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUser);

  useEffect(() => {
    if (user) localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    else localStorage.removeItem(STORAGE_KEY);
  }, [user]);

  // Đăng nhập (Luồng 2) chưa nối User Service/Keycloak — mock bằng một
  // "directory" lưu ở localStorage để nhớ vai trò (role) đã đăng ký cho mỗi email.
  const login = useCallback((email: string, role: UserRole, name?: string) => {
    const dir = loadDirectory();
    const existing = dir[email];
    const resolvedRole = existing?.role ?? role;
    const resolvedName = existing?.name ?? name ?? email.split("@")[0];
    if (!existing) {
      dir[email] = { name: resolvedName, role: resolvedRole };
      saveDirectory(dir);
    }
    setUser({ name: resolvedName, email, role: resolvedRole });
  }, []);

  const logout = useCallback(() => setUser(null), []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
