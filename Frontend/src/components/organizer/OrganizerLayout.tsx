import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Ticket,
  Plus,
  LogOut,
  ArrowLeftRight,
  Menu,
  X,
} from "lucide-react";
import { useAuth } from "@/hooks/useAuth";
import { RequireOrganizer } from "@/components/organizer/RequireOrganizer";

const NAV_ITEMS = [
  { to: "/organizer", label: "Tổng quan", icon: LayoutDashboard, end: true },
  { to: "/organizer/events", label: "Sự kiện của tôi", icon: Ticket, end: false },
];

function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="flex h-full flex-col">
      <Link
        to="/organizer"
        onClick={onNavigate}
        className="flex h-[72px] shrink-0 items-center px-6 text-2xl font-extrabold tracking-wide text-cream"
      >
        Easy<span className="text-gold">Ticket</span>
        <span className="ml-2 text-xs font-semibold text-[#8a8a80]">
          Organizer
        </span>
      </Link>

      <nav className="flex flex-1 flex-col gap-1 px-3 py-4">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            onClick={onNavigate}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-semibold transition-colors ${
                isActive
                  ? "bg-gold text-dark"
                  : "text-cream hover:bg-dark-soft"
              }`
            }
          >
            <item.icon className="h-4.5 w-4.5" />
            {item.label}
          </NavLink>
        ))}

        <Link
          to="/organizer/events/new"
          onClick={onNavigate}
          className="mt-2 flex items-center justify-center gap-2 rounded-lg border border-gold px-3 py-2.5 text-sm font-bold text-gold hover:bg-gold hover:text-dark"
        >
          <Plus className="h-4 w-4" /> Tạo sự kiện
        </Link>
      </nav>

      <div className="border-t border-dark-border px-3 py-4">
        <div className="mb-2 flex items-center gap-2.5 rounded-lg px-3 py-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-gold text-xs font-bold text-dark">
            {user?.name.slice(0, 1).toUpperCase()}
          </span>
          <div className="min-w-0">
            <div className="truncate text-sm font-semibold text-cream">
              {user?.name}
            </div>
            <div className="truncate text-xs text-[#8a8a80]">{user?.email}</div>
          </div>
        </div>
        <Link
          to="/"
          onClick={onNavigate}
          className="flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-cream hover:bg-dark-soft"
        >
          <ArrowLeftRight className="h-4 w-4" /> Về trang mua vé
        </Link>
        <button
          onClick={() => {
            logout();
            onNavigate?.();
            navigate("/");
          }}
          className="flex w-full cursor-pointer items-center gap-2.5 rounded-lg px-3 py-2 text-left text-sm text-cream hover:bg-dark-soft"
        >
          <LogOut className="h-4 w-4" /> Đăng xuất
        </button>
      </div>
    </div>
  );
}

export function OrganizerLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <RequireOrganizer>
      <div className="flex min-h-screen bg-cream text-ink">
        <aside className="hidden w-64 shrink-0 bg-dark lg:block">
          <div className="sticky top-0 h-screen">
            <SidebarContent />
          </div>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <div className="flex h-[72px] shrink-0 items-center justify-between border-b border-border bg-white px-4 sm:px-6 lg:hidden">
            <span className="text-xl font-extrabold text-ink">
              Easy<span className="text-gold">Ticket</span> Organizer
            </span>
            <button
              onClick={() => setMobileOpen(true)}
              className="cursor-pointer text-ink"
              aria-label="Mở menu"
            >
              <Menu className="h-6 w-6" />
            </button>
          </div>

          <main className="flex-1 p-4 sm:p-6 lg:p-8">
            <Outlet />
          </main>
        </div>

        {mobileOpen && (
          <div className="fixed inset-0 z-50 flex lg:hidden">
            <div className="w-72 bg-dark">
              <div className="flex h-[72px] items-center justify-end px-4">
                <button
                  onClick={() => setMobileOpen(false)}
                  className="cursor-pointer text-cream"
                  aria-label="Đóng menu"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>
              <div className="h-[calc(100%-72px)]">
                <SidebarContent onNavigate={() => setMobileOpen(false)} />
              </div>
            </div>
            <div
              className="flex-1 bg-black/40"
              onClick={() => setMobileOpen(false)}
            />
          </div>
        )}
      </div>
    </RequireOrganizer>
  );
}
