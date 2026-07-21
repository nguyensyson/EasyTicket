import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import {
  Search,
  Menu,
  X,
  User,
  LogOut,
  ClipboardList,
  LayoutDashboard,
} from "lucide-react";
import { useAuth } from "@/hooks/useAuth";

const NAV_LINKS = [
  { label: "Sự kiện", href: "/" },
];

export function Header() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("q") || "");
  const [mobileOpen, setMobileOpen] = useState(false);
  const [accountOpen, setAccountOpen] = useState(false);
  const accountRef = useRef<HTMLDivElement>(null);
  const { user, logout } = useAuth();

  useEffect(() => {
    setQuery(searchParams.get("q") || "");
  }, [searchParams]);

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (accountRef.current && !accountRef.current.contains(e.target as Node)) {
        setAccountOpen(false);
      }
    }
    document.addEventListener("mousedown", onClickOutside);
    return () => document.removeEventListener("mousedown", onClickOutside);
  }, []);

  function handleSearchSubmit(e: React.FormEvent) {
    e.preventDefault();
    navigate(query.trim() ? `/?q=${encodeURIComponent(query.trim())}` : "/");
    setMobileOpen(false);
  }

  return (
    <header className="sticky top-0 z-50 bg-dark">
      <div className="flex h-[72px] items-center gap-4 px-4 sm:px-6 lg:gap-6 lg:px-10">
        <Link
          to="/"
          className="shrink-0 text-2xl font-extrabold tracking-wide text-cream"
        >
          Easy<span className="text-gold">Ticket</span>
        </Link>

        <nav className="hidden items-center gap-7 lg:flex">
          {NAV_LINKS.map((link) => (
            <Link
              key={link.label}
              to={link.href}
              className="text-[15px] font-medium text-cream hover:text-gold"
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <form
          onSubmit={handleSearchSubmit}
          className="hidden max-w-[420px] flex-1 md:block"
        >
          <div className="relative">
            <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#8a8a80]" />
            <input
              type="text"
              placeholder="Tìm sự kiện, nghệ sĩ, địa điểm..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full rounded-lg border border-[#3a3a3a] bg-dark-soft py-2.5 pl-9 pr-3.5 text-sm text-cream outline-none placeholder:text-[#8a8a80] focus:border-gold"
            />
          </div>
        </form>

        <div className="flex-1" />

        <div className="hidden items-center gap-3 md:flex">
          {user ? (
            <div className="relative" ref={accountRef}>
              <button
                onClick={() => setAccountOpen((o) => !o)}
                className="flex cursor-pointer items-center gap-2 rounded-lg border border-transparent px-3 py-2 text-sm font-semibold text-cream hover:border-[#3a3a3a]"
              >
                <span className="flex h-7 w-7 items-center justify-center rounded-full bg-gold text-xs font-bold text-dark">
                  {user.name.slice(0, 1).toUpperCase()}
                </span>
                {user.name}
              </button>
              {accountOpen && (
                <div className="absolute right-0 top-[calc(100%+8px)] w-52 overflow-hidden rounded-lg border border-[#2a2a2a] bg-dark-soft py-1 shadow-lg">
                  {user.role === "organizer" && (
                    <Link
                      to="/organizer"
                      onClick={() => setAccountOpen(false)}
                      className="flex items-center gap-2 px-4 py-2.5 text-sm text-cream hover:bg-dark"
                    >
                      <LayoutDashboard className="h-4 w-4" /> Kênh tổ chức
                    </Link>
                  )}
                  <Link
                    to="/account/orders"
                    onClick={() => setAccountOpen(false)}
                    className="flex items-center gap-2 px-4 py-2.5 text-sm text-cream hover:bg-dark"
                  >
                    <ClipboardList className="h-4 w-4" /> Đơn hàng của tôi
                  </Link>
                  <button
                    onClick={() => {
                      logout();
                      setAccountOpen(false);
                      navigate("/");
                    }}
                    className="flex w-full cursor-pointer items-center gap-2 px-4 py-2.5 text-left text-sm text-cream hover:bg-dark"
                  >
                    <LogOut className="h-4 w-4" /> Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link
              to="/login"
              className="rounded-lg border border-gold px-4.5 py-2.5 text-sm font-semibold text-gold hover:bg-gold hover:text-dark"
            >
              Đăng nhập
            </Link>
          )}
        </div>

        <button
          onClick={() => setMobileOpen((o) => !o)}
          className="cursor-pointer text-cream md:hidden"
          aria-label="Mở menu"
        >
          {mobileOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {mobileOpen && (
        <div className="flex flex-col gap-4 border-t border-[#2a2a2a] px-4 py-5 md:hidden">
          <form onSubmit={handleSearchSubmit}>
            <div className="relative">
              <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#8a8a80]" />
              <input
                type="text"
                placeholder="Tìm sự kiện, nghệ sĩ, địa điểm..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="w-full rounded-lg border border-[#3a3a3a] bg-dark-soft py-2.5 pl-9 pr-3.5 text-sm text-cream outline-none placeholder:text-[#8a8a80] focus:border-gold"
              />
            </div>
          </form>
          <nav className="flex flex-col gap-1">
            {NAV_LINKS.map((link) => (
              <Link
                key={link.label}
                to={link.href}
                onClick={() => setMobileOpen(false)}
                className="rounded-lg px-2 py-2.5 text-[15px] font-medium text-cream hover:bg-dark-soft"
              >
                {link.label}
              </Link>
            ))}
          </nav>
          {user ? (
            <div className="flex flex-col gap-1">
              {user.role === "organizer" && (
                <Link
                  to="/organizer"
                  onClick={() => setMobileOpen(false)}
                  className="flex items-center gap-2 rounded-lg px-2 py-2.5 text-sm text-cream hover:bg-dark-soft"
                >
                  <LayoutDashboard className="h-4 w-4" /> Kênh tổ chức
                </Link>
              )}
              <Link
                to="/account/orders"
                onClick={() => setMobileOpen(false)}
                className="flex items-center gap-2 rounded-lg px-2 py-2.5 text-sm text-cream hover:bg-dark-soft"
              >
                <ClipboardList className="h-4 w-4" /> Đơn hàng của tôi
              </Link>
              <button
                onClick={() => {
                  logout();
                  setMobileOpen(false);
                  navigate("/");
                }}
                className="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-2.5 text-left text-sm text-cream hover:bg-dark-soft"
              >
                <LogOut className="h-4 w-4" /> Đăng xuất
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              onClick={() => setMobileOpen(false)}
              className="flex items-center justify-center gap-2 rounded-lg border border-gold px-4.5 py-2.5 text-sm font-semibold text-gold"
            >
              <User className="h-4 w-4" /> Đăng nhập
            </Link>
          )}
        </div>
      )}
    </header>
  );
}
